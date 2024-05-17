package com.github.observer.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Branch {

    private String name;
    private Commit commit;
}
