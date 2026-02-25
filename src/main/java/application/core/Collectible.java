package application.core;

import application.entities.Player;

public interface Collectible {
    boolean onCollect(Player player);
}