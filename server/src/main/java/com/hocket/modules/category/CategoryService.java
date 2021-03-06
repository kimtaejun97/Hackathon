package com.hocket.modules.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void initData(){
        if(categoryRepository.count() == 0){
            List<String> titles = Arrays.asList("home", "challenge", "hobby", "learning", "exercise", "food", "culture", "travel", "etc");
            List<Category> categories = makeCategoryFromTitle(titles);
            categoryRepository.saveAll(categories);

        }
    }

    private List<Category> makeCategoryFromTitle(List<String> titles) {
        List<Category> categories = titles.stream()
                .map(Category::new)
                .collect(Collectors.toList());
        return categories;
    }
}
