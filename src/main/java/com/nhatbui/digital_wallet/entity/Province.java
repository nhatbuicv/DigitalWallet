package com.nhatbui.digital_wallet.entity;

import jakarta.persistence.*;
import lombok.*;

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