package com.nhatbui.digital_bank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "provinces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Province {

    @Id
    @Column(length = 3)
    private String code;

    @Column(nullable = false, unique = true)
    private String name;
}