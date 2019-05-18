/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.owner;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 */
@RunWith(SpringRunner.class)
@WebMvcTest(OwnerController.class)
public class OwnerControllerTests {

    private static final int TEST_OWNER_ID_1 = 1;
    private static final int TEST_OWNER_ID_2 = 2;
    private static final int TEST_OWNER_ID_3 = 3;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository owners;

    private Owner george;
    private Owner maria1;
    private Owner maria2;

    @Before
    public void setup() {
        george = new Owner();
        george.setId(TEST_OWNER_ID_1);
        george.setFirstName("George");
        george.setLastName("Franklin");
        george.setAddress("110 W. Liberty St.");
        george.setCity("Madison");
        george.setTelephone("6085551023");
        given(this.owners.findById(TEST_OWNER_ID_1)).willReturn(george);

        maria1 = new Owner();
        maria1.setId(TEST_OWNER_ID_2);
        maria1.setFirstName("Maria");
        maria1.setLastName("Estaban");
        maria1.setAddress("110 W. Liberty St.");
        maria1.setCity("Madison");
        maria1.setTelephone("6085551023");
        given(this.owners.findById(TEST_OWNER_ID_2)).willReturn(maria1);

        maria2 = new Owner();
        maria2.setId(TEST_OWNER_ID_3);
        maria2.setFirstName("Maria");
        maria2.setLastName("Estaban");
        maria2.setAddress("110 W. Liberty St.");
        maria2.setCity("Madison");
        maria2.setTelephone("6085551023");
        given(this.owners.findById(TEST_OWNER_ID_3)).willReturn(maria2);
    }

    @Test
    public void testInitCreationForm() throws Exception {
        mockMvc.perform(get("/owners/new"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    public void testProcessCreationFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/new")
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("address", "123 Caramel Street")
            .param("city", "London")
            .param("telephone", "01316761638")
        )
            .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testProcessCreationFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/new")
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("city", "London")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("owner"))
            .andExpect(model().attributeHasFieldErrors("owner", "address"))
            .andExpect(model().attributeHasFieldErrors("owner", "telephone"))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    public void testInitFindForm() throws Exception {
        mockMvc.perform(get("/owners/find"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(view().name("owners/findOwners"));
    }

    @Test
    public void testProcessFindFormWithoutParameters() throws Exception {
        given(this.owners.findByFullName("", "")).willReturn(new HashSet<Owner>(Lists.newArrayList(george, maria1, maria2)));
        mockMvc.perform(get("/owners"))
            .andExpect(model().hasNoErrors())
            .andExpect(status().isOk())
            .andExpect(view().name("owners/ownersList"));
    }

    @Test
    public void testProcessFindFormFindOneOwnerByFullName() throws Exception {
        given(this.owners.findByFullName(george.getLastName(), george.getFirstName())).willReturn(new HashSet<Owner>(Lists.newArrayList(george)));
        mockMvc.perform(get("/owners")
            .param("lastName", "Franklin")
            .param("firstName", "George")
        )
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID_1));
    }

    @Test
    public void testProcessFindFormFindOneOwnerByLastName() throws Exception {
        given(this.owners.findByFullName(george.getLastName(), "")).willReturn(new HashSet<Owner>(Lists.newArrayList(george)));
        mockMvc.perform(get("/owners")
            .param("lastName", "Franklin")
            .param("firstName", "")
        )
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID_1));
    }

    @Test
    public void testProcessFindFormFindOneOwnerByFirstName() throws Exception {
        given(this.owners.findByFullName("", george.getFirstName())).willReturn(new HashSet<Owner>(Lists.newArrayList(george)));
        mockMvc.perform(get("/owners")
            .param("lastName", "")
            .param("firstName", "George")
        )
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID_1));
    }

    @Test
    public void testProcessFindFormFindMultipleOwnersByFullName() throws Exception {
        given(this.owners.findByFullName(maria1.getLastName(), maria1.getFirstName())).willReturn(new HashSet<Owner>(Lists.newArrayList(maria1, maria2)));
        mockMvc.perform(get("/owners")
            .param("lastName", "Estaban")
            .param("firstName", "Maria")
        )
            .andExpect(model().hasNoErrors())
            .andExpect(status().isOk())
            .andExpect(view().name("owners/ownersList"));
    }

    @Test
    public void testProcessFindFormFindMultipleOwnersByLastName() throws Exception {
        given(this.owners.findByFullName(maria1.getLastName(), "")).willReturn(new HashSet<Owner>(Lists.newArrayList(maria1, maria2)));
        mockMvc.perform(get("/owners")
            .param("lastName", "Estaban")
            .param("firstName", "")
        )
            .andExpect(model().hasNoErrors())
            .andExpect(status().isOk())
            .andExpect(view().name("owners/ownersList"));
    }

    @Test
    public void testProcessFindFormFindMultipleOwnersByFirstName() throws Exception {
        given(this.owners.findByFullName("", maria1.getFirstName())).willReturn(new HashSet<Owner>(Lists.newArrayList(maria1, maria2)));
        mockMvc.perform(get("/owners")
            .param("lastName", "")
            .param("firstName", "Maria")
        )
            .andExpect(model().hasNoErrors())
            .andExpect(status().isOk())
            .andExpect(view().name("owners/ownersList"));
    }

    @Test
    public void testProcessFindFormNoOwnersFound() throws Exception {
        mockMvc.perform(get("/owners")
            .param("lastName", "Unknown last name")
            .param("firstName", "Unknown first name")
        )
            .andExpect(model().hasErrors())
            .andExpect(status().isOk())
            .andExpect(view().name("owners/findOwners"));
    }

    @Test
    public void testInitUpdateOwnerForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/edit", TEST_OWNER_ID_1))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
            .andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
            .andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
            .andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
            .andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    public void testProcessUpdateOwnerFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID_1)
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("address", "123 Caramel Street")
            .param("city", "London")
            .param("telephone", "01616291589")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    public void testProcessUpdateOwnerFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID_1)
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("city", "London")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("owner"))
            .andExpect(model().attributeHasFieldErrors("owner", "address"))
            .andExpect(model().attributeHasFieldErrors("owner", "telephone"))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    public void testShowOwner() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID_1))
            .andExpect(status().isOk())
            .andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
            .andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
            .andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
            .andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
            .andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
            .andExpect(view().name("owners/ownerDetails"));
    }

}
