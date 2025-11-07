package org.example.xyawalongserver.model.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dish")
@Data
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 如果为null则是全局菜品

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "is_public")
    private Boolean isPublic = false; // 是否公开给其他用户

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Recipe> recipes = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdTime;

    // 辅助方法：添加配方
    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
        recipe.setDish(this);
    }

    // 辅助方法：移除配方
    public void removeRecipe(Recipe recipe) {
        recipes.remove(recipe);
        recipe.setDish(null);
    }


}