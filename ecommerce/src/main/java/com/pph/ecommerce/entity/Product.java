package com.pph.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product extends AbstractEntity {
    String sku;
    String name;
    String description;
    double price;
    int quantity;
    String category;
    String brand;
    double weight;
    String dimensions;
    double discount;
    List<String> images;
}
