package com.duberlyguarnizo.dummyjson.jwt_token;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    Long userId;
    String token;
    boolean revoked;
    boolean expired;
}
