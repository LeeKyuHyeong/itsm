package com.itsm.core.domain.user;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_menu_id")
    private Menu parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("sortOrder ASC")
    private List<Menu> children = new ArrayList<>();

    @Column(name = "menu_nm", nullable = false, length = 100)
    private String menuNm;

    @Column(name = "menu_url", length = 200)
    private String menuUrl;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_visible", nullable = false, length = 1)
    private String isVisible;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Builder
    public Menu(Menu parent, String menuNm, String menuUrl, String icon,
                int sortOrder, String isVisible, String status) {
        this.parent = parent;
        this.menuNm = menuNm;
        this.menuUrl = menuUrl;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.isVisible = isVisible != null ? isVisible : "Y";
        this.status = status != null ? status : "ACTIVE";
    }
}
