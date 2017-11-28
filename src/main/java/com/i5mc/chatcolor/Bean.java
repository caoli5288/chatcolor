package com.i5mc.chatcolor;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Data
@Table(name = "chatcolor")
public class Bean {

    @Id
    private UUID id;
    private int allBuy;
    private int hold;
}
