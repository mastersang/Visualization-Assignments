#version 150
//#extension GL_ARB_shading_language_420pack : require
#extension GL_ARB_explicit_attrib_location : require

#define TASK 10
#define ENABLE_OPACITY_CORRECTION 0
#define ENABLE_LIGHTNING 0
#define ENABLE_SHADOWING 0

in vec3 ray_entry_position;

layout(location = 0) out vec4 FragColor;

uniform mat4 Modelview;

uniform sampler3D volume_texture;
uniform sampler2D transfer_texture;

uniform vec3    camera_location;
uniform float   sampling_distance;
uniform float   sampling_distance_ref;
uniform float   iso_value;
uniform vec3    max_bounds;
uniform ivec3   volume_dimensions;

uniform vec3    light_position;
uniform vec3    light_ambient_color;
uniform vec3    light_diffuse_color;
uniform vec3    light_specular_color;
uniform float   light_ref_coef;

bool inside_volume_bounds(const in vec3 sampling_position) {
    return (all(greaterThanEqual(sampling_position, vec3(0.0))) && 
            all(lessThanEqual(sampling_position, max_bounds)));
}

float get_sample_data(vec3 in_sampling_pos) {
    vec3 obj_to_tex = vec3(1.0) / max_bounds;
    return texture(volume_texture, in_sampling_pos * obj_to_tex).r;
}

vec3 get_gradient(vec3 sampling_position) {
    float x = sampling_position[0];
    float y = sampling_position[1];
    float z = sampling_position[2];
    vec3 length = max_bounds / volume_dimensions;
    float dx = (get_sample_data(vec3(x + length.x, y, z)) - get_sample_data(vec3(x - length.x, y,z))) / 2;
    float dy = (get_sample_data(vec3(x , y + length.y, z)) - get_sample_data(vec3(x , y - length.y ,z))) / 2;
    float dz = (get_sample_data(vec3(x, y, z + length.z)) - get_sample_data(vec3(x , y, z - length.z))) / 2;
    vec3 grad = vec3(dx, dy, dz);
    return normalize(grad);
}

// 2.2
vec4 apply_shading(vec3 sampling_pos, vec4 color) {
    // diffuse
    vec3 light_direction = normalize(light_position - sampling_pos);
    vec3 grad = get_gradient(sampling_pos);
    float diffuse_angle = dot(grad, light_direction);
    vec3 diffusion_light = light_diffuse_color * max(0.0, diffuse_angle);
    
    // specular
    vec3 camera_direction = normalize(camera_location - sampling_pos);
    vec3 reflection = normalize(reflect(light_direction, grad));
    float specular_angle = dot(camera_direction, reflection);
    float specular = pow(specular_angle, light_ref_coef);
    vec3 specular_light = light_specular_color * specular;

    // lighting
    vec3 lighting = light_ambient_color + diffusion_light + specular_light;
    return color * vec4(lighting, 1.0);
}

// 3.1
vec4 front_to_back(vec3 sampling_pos, bool inside_volume, vec3 ray_increment) {
    vec4 result = vec4(0.0, 0.0, 0.0, 0.0);
    float transparency = 1;

    while (inside_volume)
    {   
        float s = get_sample_data(sampling_pos);

        #if ENABLE_OPACITY_CORRECTION == 1 // Opacity Correction
        #endif

        vec4 color = texture(transfer_texture, vec2(s, s));
        color *= transparency;
        result += color; 
        transparency *= (1 - color.a);

        // increment the ray sampling position      
        sampling_pos += ray_increment;

        // update the loop termination condition
        inside_volume = inside_volume_bounds(sampling_pos);
    }

    #if ENABLE_LIGHTNING == 1 // Add Shading
        result = apply_shading(sampling_pos, result);
    #endif

    return result;
}

// 3.2
vec4 back_to_front(vec3 sampling_pos, bool inside_volume, vec3 ray_increment) {
    vec4 result = vec4(0.0, 0.0, 0.0, 0.0);

    // get number of samples
    while (inside_volume)
    {   
        sampling_pos += ray_increment;
        inside_volume = inside_volume_bounds(sampling_pos);
    }
    
    sampling_pos -= ray_increment;
    inside_volume = true;

    // traverse back to front
    while (inside_volume) {
        float s = get_sample_data(sampling_pos);

        #if ENABLE_OPACITY_CORRECTION == 1 // Opacity Correction
        #endif

        vec4 color = texture(transfer_texture, vec2(s, s));
        result = color * color.a + result * (1 - color.a); 
        sampling_pos -= ray_increment;
        inside_volume = inside_volume_bounds(sampling_pos);
    }

    return result;
}

void main()
{
    /// One step trough the volume
    vec3 ray_increment = normalize(ray_entry_position - camera_location) * sampling_distance;

    /// Position in Volume
    vec3 sampling_pos = ray_entry_position + ray_increment; // test, increment just to be sure we are in the volume

    /// Init color of fragment
    vec4 dst = vec4(0.0, 0.0, 0.0, 0.0);

    /// check if we are inside volume
    bool inside_volume = inside_volume_bounds(sampling_pos);
    
    if (!inside_volume) {
        discard;
    }

    #if TASK == 10
        vec4 max_val = vec4(0.0, 0.0, 0.0, 0.0);
        
        // the traversal loop,
        // termination when the sampling position is outside volume boundarys
        // another termination condition for early ray termination is added
        while (inside_volume) 
        {      
            // get sample
            float s = get_sample_data(sampling_pos);
                    
            // apply the transfer functions to retrieve color and opacity
            vec4 color = texture(transfer_texture, vec2(s, s));
            
            // this is the example for maximum intensity projection
            max_val.r = max(color.r, max_val.r);
            max_val.g = max(color.g, max_val.g);
            max_val.b = max(color.b, max_val.b);
            max_val.a = max(color.a, max_val.a);
            
            // increment the ray sampling position
            sampling_pos += ray_increment;

            // update the loop termination condition
            inside_volume = inside_volume_bounds(sampling_pos);
        }

        dst = max_val;
    #endif 
        
    #if TASK == 11
        vec4 avg_val = vec4(0.0, 0.0, 0.0, 1.0);

        // store number of traversed points;
        int traversed_points = 0;

        // the traversal loop,
        // termination when the sampling position is outside volume boundarys
        // another termination condition for early ray termination is added
        while (inside_volume)
        {      
            // increment traversed_points by 1 each iteration
            ++traversed_points;        

            // get sample
            float s = get_sample_data(sampling_pos);
                    
            // apply the transfer functions to retrieve color and opacity
            vec4 color = texture(transfer_texture, vec2(s, s));

            // apply average intensity projection
            // calculate total using current sample, previous average and  traversed_points 
            avg_val.r = (color.r + avg_val.r * (traversed_points - 1)) / traversed_points;
            avg_val.g = (color.g + avg_val.g * (traversed_points - 1)) / traversed_points;
            avg_val.b = (color.b + avg_val.b * (traversed_points - 1)) / traversed_points;
            
            // increment the ray sampling position
            sampling_pos  += ray_increment;

            // update the loop termination condition
            inside_volume  = inside_volume_bounds(sampling_pos);
        }

        dst = avg_val;
    #endif
        
    #if TASK == 12 || TASK == 13
        // the traversal loop,
        // termination when the sampling position is outside volume boundarys
        // another termination condition for early ray termination is added

        while (inside_volume)
        {
            // get sample
            float s = get_sample_data(sampling_pos);

            if (s >= iso_value) {               
                vec3 intersection = sampling_pos;

                #if TASK == 13 // binary search
                    // start point = previous point
                    vec3 start_point = sampling_pos - ray_increment;
                    vec3 end_point = sampling_pos;
                    bool found = false;

                    // find the point where value equals iso, stops if distance is between start and end is less than 1
                    while (length(end_point - start_point) > 1) {       
                        vec3 mid_point = (end_point + start_point) / 2;
                        
                        // get sample
                        float mid_value = get_sample_data(mid_point);

                        if (mid_value >= iso_value) {         
                            intersection = mid_point;       

                            if (mid_value == iso_value) {
                                // stop searching
                                break;
                            }
                        } else if (s < iso_value) {
                            start_point = mid_point;
                        }                      
                    }           
                #endif     
                
                float intersection_value = get_sample_data(intersection);
                vec4 color = texture(transfer_texture, vec2(intersection_value, intersection_value));
                dst = color;

                #if ENABLE_LIGHTNING == 1 // Add Shading
                    dst = apply_shading(sampling_pos, color);
                #endif

                #if ENABLE_SHADOWING == 1 // Add Shadows
                    // find the direction to cast the shadow ray
                    vec3 shadow_ray_increment = normalize(sampling_pos - light_position) * sampling_distance;
                    vec3 shadow_ray_position = sampling_pos + shadow_ray_increment;
                    bool shadow_ray_inside_volume = inside_volume_bounds(shadow_ray_position);

                    while (shadow_ray_inside_volume) {
                        float shadow_value = get_sample_data(shadow_ray_position);
                        
                        if (shadow_value >= iso_value) {
                            // set color to black if shadow ray intersects with volume
                            dst = vec4(0.0, 0.0, 0.0, 0.0);
                            break;
                        }

                        shadow_ray_position += shadow_ray_increment;
                        shadow_ray_inside_volume = inside_volume_bounds(shadow_ray_position);
                    }
                #endif

                break;
            }

            // increment the ray sampling position
            sampling_pos += ray_increment;

            // update the loop termination condition
            inside_volume = inside_volume_bounds(sampling_pos);
        }  
    #endif

    #if TASK == 31
        dst = front_to_back(sampling_pos, inside_volume, ray_increment);
       // dst = back_to_front(sampling_pos, inside_volume, ray_increment);
    #endif 

    // return the calculated color value
    FragColor = dst;
}