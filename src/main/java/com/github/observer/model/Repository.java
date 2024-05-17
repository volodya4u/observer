package com.github.observer.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Repository {

    private String name;
    private Owner owner;
    private boolean fork;
}
