package com.raf.si.laboratoryservice.dto.response;

import com.raf.si.laboratoryservice.model.AnalysisParameter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UnprocessedReferralsResponse {
    private Long referralId;
    private String doctorFirstName;
    private String doctorLastName;
    private String departmentName;
    private Date creationDate;
    private String comment;
    private List<AnalysisParameter> analysisParameters;
}
