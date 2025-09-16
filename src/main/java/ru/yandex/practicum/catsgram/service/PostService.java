package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.SortOrder;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final Comparator<Post> postDataComparator = Comparator.comparing(Post::getPostDate);
    private final Map<Long, Post> posts = new HashMap<>();
    private final UserService userService;

    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Collection<Post> findAll(SortOrder sort,int from, int size) {
        return posts.values()
                .stream()
                .sorted(getComparator(sort))
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    private Comparator<Post> getComparator(SortOrder sort) {
        return sort == SortOrder.ASCENDING ? postDataComparator : postDataComparator.reversed();
    }

    public Post create(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }

        Optional<User> author = userService.findUserById(post.getAuthorId());
        if (author.isEmpty()) {
            throw new ConditionsNotMetException(String.format("Автор с id = %d не найден", post.getAuthorId()));
        }
        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException(String.format("Пост с id = %d нен найден", newPost.getId()));
    }

    public Optional<Post> findPostById(long postId) {
        return Optional.ofNullable(posts.get(postId));
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
