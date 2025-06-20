package com.example.LMS.DTO; 

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor 
@Builder
public class StudentDTO {

   
    private Long id;

  
    private String name;

    private String contact;
}
