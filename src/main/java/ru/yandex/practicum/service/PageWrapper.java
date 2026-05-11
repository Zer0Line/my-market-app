package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import java.util.List;

public class PageWrapper<T> {
    private final Page<T> delegate;

    public PageWrapper(Page<T> delegate) {
        this.delegate = delegate;
    }

    public List<T> getContent() {
        return delegate.getContent();
    }

    public int pageSize() {
        return delegate.getSize();
    }

    public int pageNumber() {
        return delegate.getNumber();
    }

    public boolean hasPrevious() {
        return delegate.hasPrevious();
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }
}