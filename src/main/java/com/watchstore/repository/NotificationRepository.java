package com.watchstore.repository;

import com.watchstore.model.Notification;
import java.util.List;

public interface NotificationRepository {
    List<Notification> findAll();
    Notification findById(long id);
    List<Notification> search(String keyword);
    boolean insert(Notification notification);
    boolean update(Notification notification);
    boolean delete(long id);
}
