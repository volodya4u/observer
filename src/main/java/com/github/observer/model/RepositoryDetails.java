package com.github.observer.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RepositoryDetails {

    private String name;
    private String owner;
    private List<BranchDetails> branches;
}
