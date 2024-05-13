import fr.yinxfox.Launcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class chip8Tests {

    private Launcher emu;

    @BeforeEach
    public void setup() {
        emu = new Launcher();
    }

    @Test
    public void coraxTest() {
        emu.loadROM("chip8/3-corax+.ch8");
    }
}
