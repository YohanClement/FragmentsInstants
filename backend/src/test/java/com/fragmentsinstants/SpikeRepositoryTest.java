package com.fragmentsinstants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase (replace =AutoConfigureTestDatabase.Replace.NONE)
public class SpikeRepositoryTest {
    @Autowired 
    private SpikeItemRepository SIR;

    @Test 
    public void spike(){
        SpikeItem item = new SpikeItem("test un");
        assertThat(item.getMyId()).isNull();

        SpikeItem premierSauvegarde = SIR.save(item);
        assertThat(premierSauvegarde.getMyId()).isNotNull();

        SpikeItem item2 = SIR.save(new SpikeItem("test deux"));
        assertThat(item2.getMyId()).isNotNull();

    }
}
