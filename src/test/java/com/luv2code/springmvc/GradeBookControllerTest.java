package com.luv2code.springmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
//import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class GradeBookControllerTest {

    private static MockHttpServletRequest request;

    @PersistenceContext
    private EntityManager entityManager;

    @Mock
    StudentAndGradeService studentCreateServiceMock;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradesDao mathGradeDao;

    @Autowired
    private ScienceGradesDao scienceGradeDao;

    @Autowired
    private HistoryGradesDao historyGradeDao;

    @Autowired
    private StudentAndGradeService studentService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private CollegeStudent student;

    @Value("${sql.script.create.student}")
    private String sqlAddStudent;

    @Value("${sql.script.create.math.grade}")
    private String sqlAddMathGrade;

    @Value("${sql.script.create.science.grade}")
    private String sqlAddScienceGrade;

    @Value("${sql.script.create.history.grade}")
    private String sqlAddHistoryGrade;

    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.script.delete.math.grade}")
    private String sqlDeleteMathGrade;

    @Value("${sql.script.delete.science.grade}")
    private String sqlDeleteScienceGrade;

    @Value("${sql.script.delete.history.grade}")
    private String sqlDeleteHistoryGrade;

    public static final MediaType APPLICATION_JSON_UTF8 = APPLICATION_JSON;

    @BeforeAll
    public static void setUp() {

        request = new MockHttpServletRequest();
        request.setParameter("firstname", "Chad");
        request.setParameter("lastname", "Derby");
        request.setParameter("emailAddress", "chad.derby@gmail.com");

    }

    @BeforeEach
    public void setupDatabase() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }

    @DisplayName("Test Get Students")
    @Test
    public void testGetStudentHttpRequest() throws Exception{

            student.setFirstname("Chad");
            student.setLastname("Derby");
            student.setEmailAddress("chad.derby@gmail.com");
            entityManager.persist(student);
            entityManager.flush();
            mockMvc.perform(MockMvcRequestBuilders.get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$",hasSize(2)));


    }


    @DisplayName("Test For Creating Student")
    @Test
    public void testCreateStudentHttpRequest() throws Exception{

        student.setFirstname("Chad");
        student.setLastname("Derby");
        student.setEmailAddress("chad.derby@gmail.com");

        mockMvc.perform(post("/")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(2)));


        CollegeStudent verifyStudent = studentDao.findByEmailAddress("chad.derby@gmail.com");

        assertNotNull(verifyStudent,"student should be valid");
    }

    @DisplayName("Test Delete Student")
    @Test
    public void testDeleteStudentHttpRequest() throws Exception{

        assertTrue(studentDao.findById(1).isPresent());

        mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}",1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$",hasSize(0)));

        assertFalse(studentDao.findById(1).isPresent());

      //  System.out.println(studentDao.findById(1));
       // System.out.println(studentDao.findById(0));
    }

    @DisplayName("Test Delete Invalid student id ")
    @Test
    public void testDeleteStudentHttpRequestErrorPage() throws Exception{

        assertFalse(studentDao.findById(0).isPresent());
        mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}",0))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status",is(404)))
                .andExpect(jsonPath("$.message",is("Student or Grade was not found")));
    }

    @DisplayName("Test Student Information")
    @Test
    public void testStudentInformationHttpRequest() throws Exception{

        Optional<CollegeStudent> student = studentDao.findById(1);

        assertTrue(student.isPresent());
        mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}",1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",is(1)))
                .andExpect(jsonPath("$.firstname",is("Eric")))
                .andExpect(jsonPath("$.lastname",is("Roby")))
                .andExpect(jsonPath("$.emailAddress",is("eric.roby@luv2code_school.com")));
    }

    @DisplayName("Student Information Not Found")
    @Test
    public void testStudentInformationHttpRequestErrorPage() throws Exception{
        assertFalse(studentDao.findById(0).isPresent());
        mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}",0))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status",is(404)))
                .andExpect(jsonPath("$.message",is("Student or Grade was not found")));
    }

    @DisplayName("Create a Valid Grade")
    @Test
    public void testCreateValidGradeHttpRequest() throws Exception{
        mockMvc.perform(post("/grades")
                .contentType(APPLICATION_JSON)
                .param("grade","85.00")
                .param("gradeType","math")
                .param("studentId","1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id",is(1)))
                .andExpect(jsonPath("$.firstname",is("Eric")))
                .andExpect(jsonPath("$.lastname",is("Roby")))
                .andExpect(jsonPath("$.emailAddress",is("eric.roby@luv2code_school.com")))
                .andExpect(jsonPath("$.studentGrades.mathGradeResults",hasSize(2)));
    }

    @DisplayName("Create Grades for invalid student id")
    @Test
    public void testCreateValidGradeHttpRequestStudentDoesNotExistEmptyResponse() throws  Exception{

        mockMvc.perform(post("/grades")
                .contentType(APPLICATION_JSON)
                .param("grade","85.00")
                .param("gradeType","math")
                .param("studentId","0"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status",is(404)))
                .andExpect(jsonPath("$.message",is("Student or Grade was not found")));
    }

    @DisplayName("Test For Invalid Grade Type")
    @Test
    public void testCreateInValidGradeHttpRequestGradeTypeDoesNotExistEmptyResponse() throws Exception{

        mockMvc.perform(post("/grades")
                .contentType(APPLICATION_JSON)
                .param("grade","85.00")
                .param("gradeType","literature")
                .param("studentId","1"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status",is(404)))
                .andExpect(jsonPath("$.message",is("Student or Grade was not found")));
    }

    @DisplayName("Delete A Valid Grade")
    @Test
    public void testDeleteAValidGradeHttpRequest() throws Exception{

        assertTrue(studentDao.findById(1).isPresent());
        mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}",1,"math"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id",is(1)))
                .andExpect(jsonPath("$.firstname",is("Eric")))
                .andExpect(jsonPath("$.lastname",is("Roby")))
                .andExpect(jsonPath("$.emailAddress",is("eric.roby@luv2code_school.com")))
                .andExpect(jsonPath("$.studentGrades.mathGradeResults",hasSize(0)));

    }
   @DisplayName("Delete Invalid Student Id")
   @Test
   public void testDeleteValidGradeHttpRequestStudentIdDoesNotExistEmptyResponse() throws Exception{

        Optional<MathGrade> mathGrade = mathGradeDao.findById(0);
        assertFalse(mathGrade.isPresent());
        mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}",2,"history"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status",is(404)))
                .andExpect(jsonPath("$.message", is("Student or Grade was not found")));

   }

   @DisplayName("Invalid GradeType")
   @Test
   public void testDeleteInvalidGradeHttpRequestForInvalidGradeType() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}",1,"literature"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status",is(404)))
                .andExpect(jsonPath("$.message",is("Student or Grade was not found")));
   }

    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }

}
