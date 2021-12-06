package org.bobocode.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bobocode.annotation.Column;
import org.bobocode.annotation.Table;

/**
 * Created by Shelupets Denys on 05.12.2021.
 */

@Table(name = "person")
@Getter
@Setter
@ToString
public class Person {
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String secondName;
    @Column(name = "id")
    private Integer id;

}
