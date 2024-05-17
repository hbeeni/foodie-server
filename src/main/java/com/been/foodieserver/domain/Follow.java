package com.been.foodieserver.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString(callSuper = true)
@Table(name = "follows")
@Entity
public class Follow extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User follower; //팔로우 하는 사용자

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User followee; //팔로우 당한 사용자

    protected Follow() {
    }

    private Follow(User follower, User followee) {
        this.follower = follower;
        this.followee = followee;
    }

    public static Follow of(User follower, User followee) {
        return new Follow(follower, followee);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Follow follow)) {
            return false;
        }

        return Objects.equals(getId(), follow.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
