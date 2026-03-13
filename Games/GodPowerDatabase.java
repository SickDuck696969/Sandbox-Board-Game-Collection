package Games;

import java.util.ArrayList;
import java.util.List;

public class GodPowerDatabase {
    
    public static class GodPower {
        public String name;
        public String timing;
        public String power;
        public String imagePath; 
        
        public GodPower(String name, String timing, String power, String imagePath) {
            this.name = name;
            this.timing = timing;
            this.power = power;
            this.imagePath = imagePath;
        }
    }

    public static List<GodPower> getGods() {
        List<GodPower> gods = new ArrayList<>();
        
        // 1. Red Hood (Nikke) - Dựa trên Hephaestus: Hỏa lực tỉa bắn dồn dập vào một mục tiêu.
        gods.add(new GodPower(
            "RED HOOD", 
            "Your Build", 
            "Your Worker may build one additional time on top of your first block, but not a dome.", 
            "images/Santorini/RedHood.png"
        ));

        // 2. Batman - Dựa trên Apollo: Sử dụng thiết bị dây móc để hoán đổi vị trí và khống chế đối thủ.
        gods.add(new GodPower(
            "BATMAN", 
            "Your Move", 
            "Your Worker may move into an opponent Worker's space by forcing their Worker to the space yours just vacated.", 
            "images/Santorini/Batman.png"
        ));

        // 3. Vegito - Dựa trên Minotaur: Sức mạnh áp đảo tuyệt đối, húc văng kẻ địch về phía sau.
        gods.add(new GodPower(
            "VEGITO", 
            "Your Move", 
            "Your Worker may move into an opponent Worker's space, if their Worker can be forced one space straight backwards to an unoccupied space.", 
            "images/Santorini/Vegito.png"
        ));

        // 4. Greninja - Dựa trên Artemis: Thể thuật Ninja siêu tốc, cho phép di chuyển lướt 2 lần liên tiếp.
        gods.add(new GodPower(
            "GRENINJA", 
            "Your Move", 
            "Your Worker may move one additional time, but not back to its initial space.", 
            "images/Santorini/Greninja.png"
        ));

        // 5. Kamen Rider Build - Dựa trên Demeter: Thiên tài vật lý "Best Match", xây dựng liên tục trên nhiều mặt trận.
        gods.add(new GodPower(
            "KAMEN RIDER BUILD", 
            "Your Build", 
            "Your Worker may build one additional time, but not on the same space.", 
            "images/Santorini/Build.png"
        ));

        // 6. Firefly (HSR) - Dựa trên Prometheus: Kích hoạt Scorched Earth Operations, thiết lập chiến trường trước khi xông pha.
        gods.add(new GodPower(
            "FIREFLY", 
            "Your Turn", 
            "If your Worker does not move up, it may build both before and after moving.", 
            "images/Santorini/Firefly.png"
        ));

        // 7. Strike Freedom Gundam - Dựa trên Athena: Bá chủ không không, dùng hệ thống DRAGOON phong tỏa không cho kẻ địch bay lên.
        gods.add(new GodPower(
            "STRIKE FREEDOM GUNDAM", 
            "Opponent's Turn", 
            "If one of your Workers moved up on your last turn, opponent Workers cannot move up this turn.", 
            "images/Santorini/StrikeFreedom.png"
        ));
        // 8. Yasuo (LOL) - Dựa trên Triton (Lướt E - Quét Kiếm)
        gods.add(new GodPower(
            "YASUO", 
            "Your Move", 
            "Each time your Worker moves into a perimeter space (edge of the board), it may immediately move again.", 
            "images/Santorini/Yasuo.png"
        ));

        // 9. Shadow the Hedgehog - Dựa trên Hermes (Chaos Control / Tốc độ chớp nhoáng)
        gods.add(new GodPower(
            "SHADOW", 
            "Your Turn", 
            "If your Workers do not move up or down, they may each move any number of times (even zero), and then either builds.", 
            "images/Santorini/Shadow.png"
        ));

        // 10. Lucy (Cyberpunk) - Dựa trên Aphrodite (Mono-wire Hack/Trói chân)
        gods.add(new GodPower(
            "LUCY", 
            "Opponent's Turn", 
            "If an opponent Worker starts its turn neighboring one of your Workers, its last move must be to a space neighboring one of your Workers.", 
            "images/Santorini/Lucy.png"
        ));

        // 11. Doflamingo (One Piece) - Dựa trên Hades (Lồng Chim - Birdcage)
        gods.add(new GodPower(
            "DOFLAMINGO", 
            "Opponent's Turn", 
            "Opponent Workers cannot move down. They are trapped in the Birdcage.", 
            "images/Santorini/Doflamingo.png"
        ));

        // 12. Venom (Marvel) - Dựa trên Ares (Hủy diệt / Ăn mòn)
        gods.add(new GodPower(
            "ANTI-VENOM", 
            "End of Turn", 
            "You may remove an unoccupied block (not dome) neighboring your unmoved Worker.", 
            "images/Santorini/AntiVenom.png"
        ));

        // 13. Giorno Giovanna (JoJo) - Dựa trên Zeus (Gold Experience - Tạo rễ cây nâng bản thân lên)
        gods.add(new GodPower(
            "GIORNO GIOVANNA", 
            "Your Build", 
            "Your Worker may build a block directly under itself, raising its level by 1.", 
            "images/Santorini/Giorno.png"
        ));

        // 14. Kaiba Seto (Yu-Gi-Oh!) - Dựa trên Atlas (Dùng tiền/Quyền lực chặn đường đối thủ)
        gods.add(new GodPower(
            "KAIBA SETO", 
            "Your Build", 
            "Your Worker may build a dome at any level, including the ground level.", 
            "images/Santorini/Kaiba.png"
        ));
        // 15. Homelander (The Boys) - Dựa trên Medusa (Tia Laser từ trên cao)
        // Loại bỏ kẻ địch ở vị trí thấp hơn và xây đè lên chúng.
        gods.add(new GodPower(
            "HOMELANDER", 
            "End of Turn", 
            "If possible, your Workers build in lower neighboring spaces that are occupied by opponent Workers, removing the opponent Workers from the game.", 
            "images/Santorini/Homelander.png"
        ));

        // 16. Omni-Man (Invincible) - Dựa trên Bia (Đấm xuyên thấu / Bạo lực vật lý)
        // Càn quét và tiêu diệt kẻ địch nằm trên đường thẳng di chuyển.
        gods.add(new GodPower(
            "OMNI-MAN", 
            "Your Move", 
            "If your Worker moves into a space and the next space in the same direction is occupied by an opponent Worker, the opponent's Worker is removed from the game.", 
            "images/Santorini/OmniMan.png"
        ));
        // 17. Swampfire (Ben 10) - Dựa trên Hestia (Sinh trưởng thực vật)
        gods.add(new GodPower(
            "SWAMPFIRE", 
            "Your Build", 
            "Your Worker may build one additional time, but this cannot be on a perimeter space.", 
            "images/Santorini/Swampfire.png"
        ));

        // 18. Optimus Prime (Transformers) - Dựa trên Hera (Bảo vệ lãnh thổ)
        gods.add(new GodPower(
            "OPTIMUS PRIME", 
            "Opponent's Turn", 
            "An opponent cannot win by moving into a perimeter space.", 
            "images/Santorini/OptimusPrime.png"
        ));

        // 19. Raphaela (TMNT) - Dựa trên Pan (Đòn ám sát từ trên cao / Nhảy lầu)
        gods.add(new GodPower(
            "RAPHAELA", 
            "Your Move", 
            "You also win if your Worker moves down two or more levels.", 
            "images/Santorini/Raphaela.png"
        ));

        // 20. Pennywise (IT) - Dựa trên Hypnus (Nỗi sợ hãi tê liệt trên cao)
        gods.add(new GodPower(
            "PENNYWISE", 
            "Opponent's Turn", 
            "If one of your opponent's Workers is higher than all of their others, it cannot move.", 
            "images/Santorini/Pennywise.png"
        ));

        // 21. Scorpion (Mortal Kombat) - Dựa trên Charon (Get over here!)
        gods.add(new GodPower(
            "SCORPION", 
            "Your Move", 
            "Before your Worker moves, you may force a neighboring opponent Worker to the space directly on the other side of your Worker, if that space is unoccupied.", 
            "images/Santorini/Scorpion.png"
        ));

        // 22. Ultraman Orb - Dựa trên Pegasus (Sức bật khổng lồ / Bay lượn)
        gods.add(new GodPower(
            "ULTRAMAN ORB", 
            "Your Move", 
            "Your Worker may move up or down any number of levels.", 
            "images/Santorini/UltramanOrb.png"
        ));

        // 23. Emperor (Armor Heroes) - Dựa trên Chronos (Uy quyền thống trị vương quốc)
        gods.add(new GodPower(
            "EMPEROR", 
            "End of Turn", 
            "You also win when there are at least five Complete Towers on the board.", 
            "images/Santorini/Emperor.png"
        ));

        // 24. Lloyd (Ninjago) - Dựa trên Poseidon (Sức mạnh Spinjitzu / Nguyên tố)
        gods.add(new GodPower(
            "LLOYD", 
            "Your Build", 
            "If your unmoved Worker is on the ground level, it may build up to three times.", 
            "images/Santorini/Lloyd.png"
        ));

        return gods;
    }
}