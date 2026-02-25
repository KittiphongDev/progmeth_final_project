package application.core;

import application.entities.Player;

public interface Collectible {
    void onCollect(Player player); // สิ่งที่จะเกิดขึ้นเมื่อ Player มาชน
}