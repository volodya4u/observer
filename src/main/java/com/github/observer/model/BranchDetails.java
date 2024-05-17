package com.github.observer.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class BranchDetails {

    private String name;
    private String lastCommitSha;
}
