package com.raf.si.patientservice.unit.service;

import com.raf.si.patientservice.dto.request.PatientRequest;
import com.raf.si.patientservice.exception.BadRequestException;
import com.raf.si.patientservice.mapper.PatientMapper;
import com.raf.si.patientservice.model.*;
import com.raf.si.patientservice.repository.*;
import com.raf.si.patientservice.service.PatientService;
import com.raf.si.patientservice.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PatientServiceTest {

    private PatientService patientService;
    private PatientRepository patientRepository;
    private HealthRecordRepository healthRecordRepository;
    private VaccinationRepository vaccinationRepository;
    private OperationRepository operationRepository;
    private MedicalHistoryRepository medicalHistoryRepository;
    private MedicalExaminationRepository medicalExaminationRepository;
    private AllergyRepository allergyRepository;
    private PatientMapper patientMapper;

    @BeforeEach
    public void setUp(){
        this.patientRepository = mock(PatientRepository.class);
        this.healthRecordRepository = mock(HealthRecordRepository.class);
        this.vaccinationRepository = mock(VaccinationRepository.class);
        this.operationRepository = mock(OperationRepository.class);
        this.medicalHistoryRepository = mock(MedicalHistoryRepository.class);
        this.medicalExaminationRepository = mock(MedicalExaminationRepository.class);
        this.allergyRepository = mock(AllergyRepository.class);
        this.patientMapper = new PatientMapper();

        this.patientService = new PatientServiceImpl(patientRepository,
                healthRecordRepository,
                vaccinationRepository,
                operationRepository,
                medicalHistoryRepository,
                medicalExaminationRepository,
                allergyRepository,
                patientMapper);
    }

    @Test
    void findPatientByJmbg_Success(){
        Patient patient = new Patient();
        String jmbg = "jmbg";
        patient.setJmbg(jmbg);

        when(patientRepository.findByJmbgAndDeleted(jmbg, false)).thenReturn(Optional.of(patient));

        assertEquals(patientService.findPatient(jmbg), patient);
    }

    @Test
    void findPatientByJmbg_PatientDoesntExist_ThrowException(){
        String jmbg = "jmbg";

        when(patientRepository.findByJmbgAndDeleted(jmbg, false)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> patientService.findPatient(jmbg));
    }

    @Test
    void findPatientByLbp_Success(){
        Patient patient = new Patient();
        UUID lbp = UUID.randomUUID();
        patient.setLbp(lbp);

        when(patientRepository.findByLbpAndDeleted(lbp, false)).thenReturn(Optional.of(patient));

        assertEquals(patientService.findPatient(lbp), patient);
    }

    @Test
    void findPatientByLbp_PatientDoesntExist_ThrowException(){
        UUID lbp = UUID.randomUUID();

        when(patientRepository.findByLbpAndDeleted(lbp, false)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> patientService.findPatient(lbp));
    }

    @Test
    void createPatientTest_Success(){
        PatientRequest patientRequest = makePatientRequest();
        patientRequest.setDeathDate(null);
        Patient patient = makePatient(patientRequest);

        when(patientRepository.findByJmbg(patient.getJmbg())).thenReturn(Optional.empty());
        when(patientRepository.save(any())).thenReturn(patient);

        assertEquals(patientService.createPatient(patientRequest), patientMapper.patientToPatientResponse(patient));
    }

    @Test
    void createPatientTest_PatientAlreadyInRepository_ThrowsException(){
        PatientRequest request = makePatientRequest();
        Patient patient = patientMapper.patientRequestToPatient(new Patient(), request);

        when(patientRepository.findByJmbg(request.getJmbg())).thenReturn(Optional.of(patient));

        assertThrows(BadRequestException.class, () -> patientService.createPatient(request));
    }

    @Test
    void createPatientTest_BirthDateAfterCurrentTime_ThrowsException(){
        PatientRequest request = makePatientRequest();
        request.setBirthDate(new Date(System.currentTimeMillis() + 10000000L));
        Patient patient = patientMapper.patientRequestToPatient(new Patient(), request);

        when(patientRepository.findByJmbg(request.getJmbg())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> patientService.createPatient(request));
    }

    @Test
    void createPatientTest_DeathDateAfterCurrentTime_ThrowsException(){
        PatientRequest request = makePatientRequest();
        request.setDeathDate(new Date(System.currentTimeMillis() + 10000000L));
        Patient patient = patientMapper.patientRequestToPatient(new Patient(), request);

        when(patientRepository.findByJmbg(request.getJmbg())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> patientService.createPatient(request));
    }

    @Test
    void createPatientTest_BirthDateAfterDeathDate_ThrowsException(){
        PatientRequest request = makePatientRequest();
        request.setBirthDate(new Date(System.currentTimeMillis() - 10000000L));
        request.setDeathDate(new Date(request.getBirthDate().getTime() - 10000L));
        Patient patient = patientMapper.patientRequestToPatient(new Patient(), request);

        when(patientRepository.findByJmbg(request.getJmbg())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> patientService.createPatient(request));
    }

    @Test
    void createPatientTest_GenderDoesntExist_ThrowsException(){
        PatientRequest request = makePatientRequest();
        request.setGender("srednji");

        when(patientRepository.findByJmbg(request.getJmbg())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> patientService.createPatient(request));
    }

    @Test
    void createPatientTest_MaritalStatusDoesntExist_ThrowsException(){
        PatientRequest request = makePatientRequest();
        request.setMaritalStatus("marital");

        when(patientRepository.findByJmbg(request.getJmbg())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> patientService.createPatient(request));
    }

    @Test
    void createPatientTest_FamilyStatusDoesntExist_ThrowsException(){
        PatientRequest request = makePatientRequest();
        request.setFamilyStatus("family");

        when(patientRepository.findByJmbg(request.getJmbg())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> patientService.createPatient(request));
    }

    @Test
    void createPatientTest_EducationDoesntExist_ThrowsException(){
        PatientRequest request = makePatientRequest();
        request.setEducation("education");

        when(patientRepository.findByJmbg(request.getJmbg())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> patientService.createPatient(request));
    }

    @Test
    void createPatientTest_CountryOfLivingDoesntExist_ThrowsException(){
        PatientRequest request = makePatientRequest();
        request.setCountryOfLiving("country");

        when(patientRepository.findByJmbg(request.getJmbg())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> patientService.createPatient(request));
    }

    @Test
    void createPatientTest_CitizenshipCountryDoesntExist_ThrowsException(){
        PatientRequest request = makePatientRequest();
        request.setCitizenshipCountry("country");

        when(patientRepository.findByJmbg(request.getJmbg())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> patientService.createPatient(request));
    }

    @Test
    void updatePatientByJmbgTest_Success(){
        PatientRequest patientRequest = makePatientRequest();
        Patient patient = makePatient(patientRequest);

        when(patientRepository.findByJmbgAndDeleted(patient.getJmbg(), false))
                .thenReturn(Optional.of(patient));
        when(patientRepository.save(any())).thenReturn(patient);

        assertEquals(patientService.updatePatientByJmbg(patientRequest),
                patientMapper.patientToPatientResponse(patient));
    }

    @Test
    void updatePatientByLbpTest_Success(){
        PatientRequest patientRequest = makePatientRequest();
        Patient patient = makePatient(patientRequest);

        when(patientRepository.findByLbpAndDeleted(patient.getLbp(), false))
                .thenReturn(Optional.of(patient));
        when(patientRepository.save(any())).thenReturn(patient);

        assertEquals(patientService.updatePatientByLbp(patientRequest, patient.getLbp()),
                patientMapper.patientToPatientResponse(patient));
    }

    @Test
    void deletePatientTest_Success(){
        Patient patient = makePatient();
        patient.setDeleted(true);

        when(patientRepository.findByLbpAndDeleted(patient.getLbp(), false))
                .thenReturn(Optional.of(patient));

        when(allergyRepository.updateDeletedByHealthRecord(any())).thenReturn(0);
        when(operationRepository.updateDeletedByHealthRecord(any())).thenReturn(0);
        when(medicalExaminationRepository.updateDeletedByHealthRecord(any())).thenReturn(0);
        when(medicalHistoryRepository.updateDeletedByHealthRecord(any())).thenReturn(0);
        when(vaccinationRepository.updateDeletedByHealthRecord(any())).thenReturn(0);

        when(patientRepository.save(any())).thenReturn(patient);

        assertEquals(patientService.deletePatient(patient.getLbp()),
                patientMapper.patientToPatientResponse(patient));
    }

    @Test
    void getPatientByLbpTest_Success(){
        Patient patient = makePatient();

        when(patientRepository.findByLbpAndDeleted(patient.getLbp(), false))
                .thenReturn(Optional.of(patient));

        assertEquals(patientService.getPatientByLbp(patient.getLbp()),
                patientMapper.patientToPatientResponse(patient));
    }

    @Test
    void getPatientsTest_Success(){
        Patient patient = makePatient();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Patient> page = new PageImpl<>(Arrays.asList(new Patient[] {patient}));

        when(patientRepository.findAll((Specification<Patient>) any(), (Pageable) any()))
                .thenReturn(page);

        assertEquals(patientService.getPatients(patient.getLbp(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getJmbg(),
                false,
                pageable), patientMapper.patientPageToPatientListResponse(page));
    }




    private PatientRequest makePatientRequest() {
        PatientRequest patientRequest = new PatientRequest();

        patientRequest.setJmbg("1342002345612");
        patientRequest.setFirstName("Pacijent");
        patientRequest.setLastName("Pacijentovic");
        patientRequest.setParentName("Roditelj");
        patientRequest.setGender("Muški");
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        try {
            patientRequest.setBirthDate(formatter.parse("12-12-1976"));
            patientRequest.setDeathDate(formatter.parse("12-12-2020"));
        }catch(Exception e){
            return null;
        }
        patientRequest.setBirthplace("Resnjak");
        patientRequest.setCitizenshipCountry("SRB");
        patientRequest.setCountryOfLiving("AFG");

        patientRequest.setAddress("Jurija Gagarina 16");
        patientRequest.setPlaceOfLiving("Novi Beograd");
        patientRequest.setPhoneNumber("0601234567");
        patientRequest.setEmail("pacijent.pacijentovic@gmail.com");
        patientRequest.setCustodianJmbg("0101987123456");
        patientRequest.setCustodianName("Staratelj Starateljovic");
        patientRequest.setProfession("Programer");
        patientRequest.setChildrenNum(2);
        patientRequest.setEducation("Osnovno obrazovanje");
        patientRequest.setMaritalStatus("Razveden");
        patientRequest.setFamilyStatus("Usvojen");

        return patientRequest;
    }

    private Patient makePatient(PatientRequest patientRequest){
        HealthRecord healthRecord = makeHealthRecord();
        Patient patient = new Patient();
        patient.setHealthRecord(healthRecord);
        return patientMapper.patientRequestToPatient(patient, patientRequest);
    }

    private Patient makePatient(){
        HealthRecord healthRecord = makeHealthRecord();
        Patient patient = new Patient();
        patient.setHealthRecord(healthRecord);
        return patient;
    }

    private HealthRecord makeHealthRecord(){
        HealthRecord healthRecord = new HealthRecord();

        Allergy allergy = new Allergy();
        Operation operation = new Operation();
        MedicalHistory history = new MedicalHistory();
        MedicalExamination examination = new MedicalExamination();
        Vaccination vaccination = new Vaccination();

        List<Allergy> allergies = Arrays.asList(new Allergy[] {allergy});
        List<Operation> operations = Arrays.asList(new Operation[] {operation});
        List<MedicalHistory> medicalHistoryList = Arrays.asList(new MedicalHistory[] {history});
        List<MedicalExamination> examinations = Arrays.asList(new MedicalExamination[] {examination});
        List<Vaccination> vaccinations = Arrays.asList(new Vaccination[] {vaccination});

        healthRecord.setAllergies(allergies);
        healthRecord.setOperations(operations);
        healthRecord.setMedicalExaminations(examinations);
        healthRecord.setMedicalHistory(medicalHistoryList);
        healthRecord.setVaccinations(vaccinations);

        return healthRecord;
    }
}
