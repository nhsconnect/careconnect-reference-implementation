package uk.nhs.careconnect.ri.database.entity;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BaseHumanNameTest {

    @Test
    public void testDisplayName_AllFields(){
        BaseHumanName name = new BaseHumanName();
        name.setPrefix("Mr");
        name.setGivenName("Jack");
        name.setFamilyName("Johnson");
        name.setSuffix("Esq");

        String displayName = name.getDisplayName();
        assertThat(displayName, not(nullValue()));
        assertThat(displayName, equalTo("Mr Jack Johnson Esq"));
    }

    @Test
    public void testDisplayName_MissingSuffix(){
        BaseHumanName name = new BaseHumanName();
        name.setPrefix("Mr");
        name.setGivenName("Jack");
        name.setFamilyName("Johnson");

        String displayName = name.getDisplayName();
        assertThat(displayName, not(nullValue()));
        assertThat(displayName, equalTo("Mr Jack Johnson"));
    }

    @Test
    public void testDisplayName_MissingTitle(){
        BaseHumanName name = new BaseHumanName();
        name.setGivenName("Jack");
        name.setFamilyName("Johnson");
        name.setSuffix("Esq");

        String displayName = name.getDisplayName();
        assertThat(displayName, not(nullValue()));
        assertThat(displayName, equalTo("Jack Johnson Esq"));
    }

    @Test
    public void testDisplayName_Empty(){
        BaseHumanName name = new BaseHumanName();

        String displayName = name.getDisplayName();
        assertThat(displayName, not(nullValue()));
        assertThat(displayName, equalTo(""));
    }


}