package com.ssafy.fleaOn.web.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", updatable = false)
    private int productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="price", nullable = false)
    private int price;

    @Column(name="first_category", nullable = false)
    private int firstCategoryId;

    @Column(name="second_category", nullable = false)
    private int secondCategoryId;

    @Column(name = "current_buyer_id", nullable = false)
    private int currentBuyerId;

    @Column(name = "reservation_count", nullable = false)
    private int reservationCount;

    @Column(name = "start", nullable = false)
    private boolean start;

    @Column(name = "end", nullable = false)
    private boolean end;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_id", nullable = false)
    private Live live;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Shorts shorts;

    @Builder
    public Product(Live live, User user, String name, int price, int firstCategoryId, int secondCategoryId) {
        this.live = live;
        this.seller = user;
        this.name = name;
        this.price = price;
        this.firstCategoryId = firstCategoryId;
        this.secondCategoryId = secondCategoryId;
        this.currentBuyerId = 0;
        this.reservationCount = 0;
        this.start = false;
        this.end = false;
    }

    public void update(String name, int price, int firstCategoryId, int secondCategoryId) {
        this.name = name;
        this.price = price;
        this.firstCategoryId = firstCategoryId;
        this.secondCategoryId = secondCategoryId;
    }

    public void sellStart(){
        this.start = true;
    }

    public void sellEnd(){
        this.end = true;
    }

    public void discount(){
        this.price = (int)(this.price*0.9);
    }
}
