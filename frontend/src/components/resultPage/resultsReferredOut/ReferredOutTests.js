import React, { useContext, useState, useRef, useEffect } from "react";
import { FormattedMessage, injectIntl, useIntl } from "react-intl";
import "../../Style.css";
import { getFromOpenElisServer } from "../../utils/Utils";
import {
  Form,
  FormLabel,
  Checkbox,
  Dropdown,
  Heading,
  Grid,
  Column,
  Section,
  TextInput,
  Button,
  DatePicker,
  DatePickerInput,
  RadioButton,
  RadioButtonGroup,
  Loading,
  DataTable,
  Pagination,
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableHeader,
  TableBody,
  TableCell,
  Select,
  SelectItem,
} from "@carbon/react";
import CustomLabNumberInput from "../../common/CustomLabNumberInput";
import config from "../../../config.json";
import { patientSearchHeaderData } from "../../data/PatientResultsTableHeaders";
import CustomDatePicker from "../../common/CustomDatePicker";
import PageBreadCrumb from "../../common/PageBreadCrumb";
import AutoComplete from "../../common/AutoComplete";
import { ConfigurationContext } from "../../layout/Layout";
import { Formik, Field } from "formik";
import ReferredOutTestsFormValues from "../../formModel/innitialValues/ReferredOutTestsFormValues";
import { NotificationContext } from "../../layout/Layout";
import {
  AlertDialog,
  NotificationKinds,
} from "../../common/CustomNotification";

let breadcrumbs = [
  { label: "home.label", link: "/" },
  { label: "referral.label.referredOutTests", link: "/ReferredOutTests" },
];

function ReferredOutTests(props) {
  // NAMEING fix
  const [reportFormValues, setReportFormValues] = useState(
    ReferredOutTestsFormValues
  );
  // const { reportFormValues, setReportFormValues, getSelectedPatient, samples, error } = props;
  const { configurationProperties } = useContext(ConfigurationContext);
  const { notificationVisible, setNotificationVisible, addNotification } =
    useContext(NotificationContext);

  const intl = useIntl();
  const itemList = [
    {
      id: "option-0",
      text: "Sent Date",
      tag: "SENT_DATE",
    },
    {
      id: "option-1",
      text: "Result Date",
      tag: "RESULT_DATE",
    },
  ];

  const componentMounted = useRef(false);
  const [checkbox, setCheckbox] = useState("on");
  const [result, setResult] = useState("false");
  const [items, setItems] = useState(itemList[0].tag);
  const [dob, setDob] = useState("");
  const [patientSearchResults, setPatientSearchResults] = useState([]);
  const [page, setPage] = useState(1);
  const [siteNames, setSiteNames] = useState([]);
  const [pageSize, setPageSize] = useState(5);
  const [loading, setLoading] = useState(false);
  const [nextPage, setNextPage] = useState(null);
  const [previousPage, setPreviousPage] = useState(null);
  const [pagination, setPagination] = useState(false);
  const [url, setUrl] = useState("");
  const [innitialized, setInnitialized] = useState(false);
  const [departments, setDepartments] = useState([]);
  const [tests, setTests] = useState([]);
  const [testSections, setTestSections] = useState([]);
  const [defaultTestSectionId, setDefaultTestSectionId] = useState("");
  const [defaultTestSectionLabel, setDefaultTestSectionLabel] = useState("");
  const [defaultTestId, setDefaultTestId] = useState("");
  const [defaultTestLabel, setDefaultTestLabel] = useState("");

  // search endpoint fix needed
  const handleSearchReferralPatient = () => {
    let barcodesPdf =
      config.serverBaseUrl +
      `/ReferredOutTests?report=patientCILNSP_vreduit&type=patient&accessionDirect=${reportFormValues.form}&highAccessionDirect=${reportFormValues.to}&dateOfBirthSearchValue=${reportFormValues.dateOfBirth}&selPatient=${reportFormValues.selectedPatientId}&referringSiteId=${reportFormValues.referringSiteId}&referringSiteDepartmentId=${reportFormValues.referringSiteName}&onlyResults=${result}&_onlyResults=${checkbox}&dateType=${items}&lowerDateRange=${reportFormValues.startDate}&upperDateRange=${reportFormValues.endDate}`;
    window.open(barcodesPdf);
  };

  //ReferredOutTests?searchType=PATIENT&dateType=SENT&startDate=&endDate=&_testUnitIds=1&_testIds=1&labNumber=DEV01240000000000003&dateOfBirthSearchValue=&selPatient=4&_csrf=f6b25f58-8652-4119-899f-b25fff72345e

  const handleSubmit = (values) => {
    setLoading(true);
    values.dateOfBirth = dob;
    //input fix needed
    const searchEndPoint =
      "/rest/patient-search-results?" +
      "&lastName=" +
      values.lastName +
      "&firstName=" +
      values.firstName +
      "&STNumber=" +
      values.patientId +
      "&subjectNumber=" +
      values.patientId +
      "&nationalID=" +
      values.patientId +
      "&labNumber=" +
      values.labNumber +
      "&guid=" +
      values.guid +
      "&dateOfBirth=" +
      values.dateOfBirth +
      "&gender=" +
      values.gender;
    getFromOpenElisServer(searchEndPoint, fetchPatientResults);
    setUrl(searchEndPoint);
  };

  function encodeDate(dateString) {
    if (typeof dateString === "string" && dateString.trim() !== "") {
      return dateString.split("/").map(encodeURIComponent).join("%2F");
    } else {
      return "";
    }
  }

  function handlePatientIdFrom(e) {
    setReportFormValues({
      ...reportFormValues,
      form: e.target.value,
    });
  }

  function handlePatientIdTo(e) {
    setReportFormValues({
      ...reportFormValues,
      to: e.target.value,
    });
  }

  function handleLabNumber(e) {
    setReportFormValues({
      ...reportFormValues,
      labNumber: e.target.value,
    });
  }
  function handlePatientId(e) {
    setReportFormValues({
      ...reportFormValues,
      patientId: e.target.value,
    });
  }

  function handleLastName(e) {
    setReportFormValues({
      ...reportFormValues,
      lastName: e.target.value,
    });
  }

  function handleFirstName(e) {
    setReportFormValues({
      ...reportFormValues,
      firstName: e.target.value,
    });
  }

  function handleGender(e) {
    setReportFormValues({
      ...reportFormValues,
      gender: e,
    });
  }

  function handleSiteName(e) {
    setReportFormValues({
      ...reportFormValues,
      referringSiteName: e.target.value,
    });
  }

  function handleRequesterDept(e) {
    setReportFormValues({
      ...reportFormValues,
      referringSiteDepartmentId: e.target.value,
    });
  }

  function handleAutoCompleteSiteName(siteId) {
    setReportFormValues({
      ...reportFormValues,
      referringSiteId: siteId,
      referringSiteName: "",
    });
  }
  const loadDepartments = (data) => {
    setDepartments(data);
  };

  const loadNextResultsPage = () => {
    setLoading(true);
    getFromOpenElisServer(url + "&page=" + nextPage, fetchPatientResults);
  };

  const loadPreviousResultsPage = () => {
    setLoading(true);
    getFromOpenElisServer(url + "&page=" + previousPage, fetchPatientResults);
  };

  const fetchPatientResults = (res) => {
    let patientsResults = res.patientSearchResults;
    if (patientsResults.length > 0) {
      patientsResults.forEach((item) => (item.id = item.patientID));
      setPatientSearchResults(patientsResults);
    } else {
      setPatientSearchResults([]);
      addNotification({
        title: intl.formatMessage({ id: "notification.title" }),
        message: intl.formatMessage({ id: "patient.search.nopatient" }),
        kind: NotificationKinds.warning,
      });
      setNotificationVisible(true);
    }
    if (res.paging) {
      var { totalPages, currentPage } = res.paging;
      if (totalPages > 1) {
        setPagination(true);
        if (parseInt(currentPage) < parseInt(totalPages)) {
          setNextPage(parseInt(currentPage) + 1);
        } else {
          setNextPage(null);
        }
        if (parseInt(currentPage) > 1) {
          setPreviousPage(parseInt(currentPage) - 1);
        } else {
          setPreviousPage(null);
        }
      }
    }
    setLoading(false);
  };

  const fetchPatientDetails = (patientDetails) => {
    props.getSelectedPatient(patientDetails);
  };

  const handleDatePickerChange = (...e) => {
    let updatedDate = encodeDate(e[1]);

    setReportFormValues({
      ...reportFormValues,
      dateOfBirth: updatedDate,
    });

    setDob(e[1]);
  };

  const handleDatePickerChangeDate = (datePicker, date) => {
    let updatedDate = encodeDate(date);
    let obj = null;
    switch (datePicker) {
      case "startDate":
        obj = {
          ...reportFormValues,
          startDate: updatedDate,
        };
        break;
      case "endDate":
        obj = {
          ...reportFormValues,
          endDate: updatedDate,
        };
        break;
      default:
    }
    setReportFormValues({
      ...reportFormValues,
      PatientStatusReportFormValues: obj,
    });
  };

  const patientSelected = (e) => {
    const patientSelected = patientSearchResults.find((patient) => {
      return patient.patientID == e.target.id;
    });

    setReportFormValues({
      ...reportFormValues,
      selectedPatientId: e.target.id,
    });

    const searchEndPoint =
      "/rest/patient-details?patientID=" + patientSelected.patientID;
    getFromOpenElisServer(searchEndPoint, fetchPatientDetails);
  };

  const getSampleEntryPreform = (response) => {
    if (componentMounted.current) {
      setSiteNames(response.sampleOrderItems.referringSiteList);
    }
  };

  const handlePageChange = (pageInfo) => {
    if (page != pageInfo.page) {
      setPage(pageInfo.page);
    }

    if (pageSize != pageInfo.pageSize) {
      setPageSize(pageInfo.pageSize);
    }
  };

  const fetchTestSections = (response) => {
    setTestSections(response);
  };

  const getTests = (tests) => {
    if (componentMounted.current) {
      setTests(tests);
    }
  };

  const submitOnSelect = (e) => {
    setNextPage(null);
    setPreviousPage(null);
    setPagination(false);
    var values = { unitType: e.target.value };
    handleSubmit(values);
  };

  useEffect(() => {
    getFromOpenElisServer(
      "/rest/departments-for-site?refferingSiteId=" +
        (reportFormValues.referringSiteId || ""),
      loadDepartments
    );
  }, [reportFormValues.referringSiteId]);

  useEffect(() => {
    componentMounted.current = true;
    getFromOpenElisServer("/rest/SamplePatientEntry", getSampleEntryPreform);
    window.scrollTo(0, 0);
    return () => {
      componentMounted.current = false;
    };
  }, []);

  useEffect(() => {
    //unit(s)
    componentMounted.current = true;
    let testId = new URLSearchParams(window.location.search).get(
      "selectedTest"
    );
    testId = testId ? testId : "";
    getFromOpenElisServer("/rest/test-list", (fetchedTests) => {
      let test = fetchedTests.find((test) => test.id === testId);
      let testLabel = test ? test.value : "";
      setDefaultTestId(testId);
      setDefaultTestLabel(testLabel);
      getTests(fetchedTests);
    });

    //test(s)
    let testSectionId = new URLSearchParams(window.location.search).get(
      "testSectionId"
    );
    testSectionId = testSectionId ? testSectionId : "";
    getFromOpenElisServer("/rest/user-test-sections", (fetchedTestSections) => {
      let testSection = fetchedTestSections.find(
        (testSection) => testSection.id === testSectionId
      );
      let testSectionLabel = testSection ? testSection.value : "";
      setDefaultTestSectionId(testSectionId);
      setDefaultTestSectionLabel(testSectionLabel);
      fetchTestSections(fetchedTestSections);
    });
    if (testSectionId) {
      let values = { unitType: testSectionId };
      querySearch(values);
    }
  }, []);

  useEffect(() => {
    let patientId = new URLSearchParams(window.location.search).get(
      "patientId"
    );
    if (patientId) {
      let searchValues = {
        ...reportFormValues,
        patientId: patientId,
      };
      setReportFormValues(searchValues);
      handleSubmit(searchValues);
    }
  }, [reportFormValues]);

  useEffect(() => {
    if (!innitialized) {
      let updatedDate = encodeDate(configurationProperties.currentDateAsText);
      setReportFormValues({
        ...reportFormValues,
        dateOfBirth: updatedDate,
        startDate: updatedDate,
        endDate: updatedDate,
      });
    }
    if (reportFormValues.dateOfBirth != "") {
      setInnitialized(true);
    }
  }, [
    reportFormValues,
    innitialized,
    configurationProperties.currentDateAsText,
  ]);

  return (
    <>
      <PageBreadCrumb breadcrumbs={breadcrumbs} />
      <Grid fullWidth={true}>
        <Column lg={16}>
          <Section>
            <Section>
              <Heading>
                <FormattedMessage id="referral.out.head" />
              </Heading>
            </Section>
          </Section>
        </Column>
      </Grid>

      {notificationVisible === true ? <AlertDialog /> : ""}
      {loading && <Loading />}
      <div className="orderLegendBody">
        <Formik
          initialValues={reportFormValues}
          enableReinitialize={true}
          // validationSchema={}
          onSubmit={handleSubmit}
          onChange
        >
          {({
            values,
            //errors,
            //touched,
            setFieldValue,
            handleChange,
            handleBlur,
            handleSubmit,
          }) => (
            <Form
              onSubmit={handleSubmit}
              onChange={handleChange}
              onBlur={handleBlur}
            >
              <Field name="guid">
                {({ field }) => (
                  <input type="hidden" name={field.name} id={field.name} />
                )}
              </Field>
              <Grid fullWidth={true}>
                <Column lg={16} md={8} sm={4}>
                  <Section>
                    <div className="inlineDiv">
                      <h5 style={{ paddingTop: "10px", paddingRight: "6px" }}>
                        <FormattedMessage id="referral.out.request" />
                      </h5>
                      <Dropdown
                        id="dateType"
                        name="dateType"
                        initialSelectedItem={itemList.find(
                          (item) => item.tag === items
                        )}
                        items={itemList}
                        itemToString={(item) => (item ? item.text : "")}
                        onChange={(item) => {
                          setItems(item.selectedItem.tag);
                        }}
                      />
                      <h5 style={{ paddingTop: "10px", paddingLeft: "6px" }}>
                        <FormattedMessage id="referral.out.note" />
                      </h5>
                    </div>
                    <div className="formInlineDiv">
                      <CustomDatePicker
                        id={"startDate"}
                        labelText={intl.formatMessage({
                          id: "eorder.date.start",
                          defaultMessage: "Start Date",
                        })}
                        autofillDate={true}
                        value={reportFormValues.startDate}
                        className="inputDate"
                        onChange={(date) =>
                          handleDatePickerChangeDate("startDate", date)
                        }
                      />
                      <CustomDatePicker
                        id={"endDate"}
                        labelText={intl.formatMessage({
                          id: "eorder.date.end",
                          defaultMessage: "End Date",
                        })}
                        className="inputDate"
                        autofillDate={true}
                        value={reportFormValues.endDate}
                        onChange={(date) =>
                          handleDatePickerChangeDate("endDate", date)
                        }
                      />
                    </div>
                    <div className="inlineDiv">
                      <Grid fullWidth={true}>
                        <Column lg={6} md={4} sm={2}>
                          <Select
                            labelText={intl.formatMessage({
                              id: "search.label.testunit",
                            })}
                            name="unitType"
                            id="unitType"
                            onChange={submitOnSelect}
                          >
                            <SelectItem
                              text={defaultTestSectionLabel}
                              value={defaultTestSectionId}
                            />
                            {testSections
                              .filter(
                                (item) => item.id !== defaultTestSectionId
                              )
                              .map((test, index) => {
                                return (
                                  <SelectItem
                                    key={index}
                                    text={test.value}
                                    value={test.id}
                                  />
                                );
                              })}
                          </Select>
                        </Column>
                        <Column lg={10} md={4} sm={2}>
                          <Field name="testName">
                            {({ field }) => (
                              <Select
                                labelText={
                                  <FormattedMessage id="search.label.test" />
                                }
                                name={field.name}
                                id={field.name}
                              >
                                <SelectItem
                                  text={defaultTestLabel}
                                  value={defaultTestId}
                                />
                                {tests
                                  .filter((item) => item.id !== defaultTestId)
                                  .map((test, index) => {
                                    return (
                                      <SelectItem
                                        key={index}
                                        text={test.value}
                                        value={test.id}
                                      />
                                    );
                                  })}
                              </Select>
                            )}
                          </Field>
                        </Column>
                      </Grid>
                    </div>
                    <div className="formInlineDiv">
                      <div className="searchActionButtons">
                        <Button type="submit">
                          <FormattedMessage
                            id="label.button.search"
                            defaultMessage="Search"
                          />
                        </Button>
                      </div>
                    </div>
                  </Section>
                </Column>
              </Grid>
              <hr />
              <Grid fullWidth={true}>
                <Column lg={16} md={8} sm={4}>
                  <Section>
                    <div className="formInlineDiv">
                      <h5>
                        <FormattedMessage id="referral.result.labnumber" />
                      </h5>
                    </div>
                    <br />
                    <Field name="labNumber">
                      {({ field }) => (
                        <CustomLabNumberInput
                          name={field.name}
                          labelText={intl.formatMessage({
                            id: "referral.input",
                            defaultMessage: "Scan OR Enter Manually",
                          })}
                          id={field.name}
                          className="inputText"
                          value={values[field.name]}
                          onChange={(e, rawValue) => {
                            setFieldValue(field.name, rawValue);
                            handleLabNumber(e);
                          }}
                        />
                      )}
                    </Field>
                    <br />
                    <div className="formInlineDiv">
                      <div className="searchActionButtons">
                        <Button type="submit">
                          <FormattedMessage
                            id="label.button.search"
                            defaultMessage="Search"
                          />
                        </Button>
                      </div>
                    </div>
                  </Section>
                </Column>
              </Grid>
              <hr />
              <Grid fullWidth={true}>
                <Column lg={16} md={8} sm={4}>
                  <Section>
                    <h5>
                      <FormattedMessage id="referral.search" />
                    </h5>
                    <div className="formInlineDiv">
                      <Field name="labNumber">
                        {({ field }) => (
                          <CustomLabNumberInput
                            name={field.name}
                            labelText={intl.formatMessage({
                              id: "eorder.labNumber",
                              defaultMessage: "Lab Number",
                            })}
                            id={field.name}
                            className="inputText"
                            value={values[field.name]}
                            onChange={(e, rawValue) => {
                              setFieldValue(field.name, rawValue);
                              handleLabNumber(e);
                            }}
                          />
                        )}
                      </Field>
                      <Field name="patientId">
                        {({ field }) => (
                          <TextInput
                            name={field.name}
                            value={values[field.name]}
                            labelText={intl.formatMessage({
                              id: "patient.id",
                              defaultMessage: "Patient Id",
                            })}
                            id={field.name}
                            className="inputText"
                            onChange={handlePatientId}
                          />
                        )}
                      </Field>
                      <Field name="lastName">
                        {({ field }) => (
                          <TextInput
                            name={field.name}
                            labelText={intl.formatMessage({
                              id: "patient.last.name",
                              defaultMessage: "Last Name",
                            })}
                            id={field.name}
                            className="inputText"
                            onChange={handleLastName}
                          />
                        )}
                      </Field>
                      <Field name="firstName">
                        {({ field }) => (
                          <TextInput
                            name={field.name}
                            labelText={intl.formatMessage({
                              id: "patient.first.name",
                              defaultMessage: "First Name",
                            })}
                            id={field.name}
                            className="inputText"
                            onChange={handleFirstName}
                          />
                        )}
                      </Field>
                      <Field name="dateOfBirth">
                        {({ field }) => (
                          <DatePicker
                            onChange={handleDatePickerChange}
                            name={field.name}
                            dateFormat="d/m/Y"
                            datePickerType="single"
                            light={true}
                            className="inputText"
                          >
                            <DatePickerInput
                              id="date-picker-default-id"
                              placeholder="dd/mm/yyyy"
                              labelText={intl.formatMessage({
                                id: "patient.dob",
                                defaultMessage: "Date of Birth",
                              })}
                              type="text"
                              name={field.name}
                            />
                          </DatePicker>
                        )}
                      </Field>
                      <Field name="gender">
                        {({ field }) => (
                          <RadioButtonGroup
                            className="inputText"
                            defaultSelected=""
                            legendText={intl.formatMessage({
                              id: "patient.gender",
                              defaultMessage: "Gender",
                            })}
                            name={field.name}
                            id="search_patient_gender"
                            onChange={handleGender}
                          >
                            <RadioButton
                              id="search-radio-1"
                              labelText={intl.formatMessage({
                                id: "patient.male",
                                defaultMessage: "Male",
                              })}
                              value="M"
                            />
                            <RadioButton
                              id="search-radio-2"
                              labelText={intl.formatMessage({
                                id: "patient.female",
                                defaultMessage: "Female",
                              })}
                              value="F"
                            />
                          </RadioButtonGroup>
                        )}
                      </Field>
                    </div>
                    <div className="formInlineDiv">
                      <div className="searchActionButtons">
                        <Button type="submit">
                          <FormattedMessage
                            id="label.button.search"
                            defaultMessage="Search"
                          />
                        </Button>
                      </div>
                    </div>
                  </Section>
                </Column>
              </Grid>
              <Column lg={16}>
                {pagination && (
                  <Grid>
                    <Column lg={11} />
                    <Column lg={2}>
                      <Button
                        type=""
                        id="loadpreviousresults"
                        onClick={loadPreviousResultsPage}
                        disabled={previousPage != null ? false : true}
                      >
                        <FormattedMessage id="button.label.loadprevious" />
                      </Button>
                    </Column>
                    <Column lg={2}>
                      <Button
                        type=""
                        id="loadnextresults"
                        disabled={nextPage != null ? false : true}
                        onClick={loadNextResultsPage}
                      >
                        <FormattedMessage id="button.label.loadnext" />
                      </Button>
                    </Column>
                  </Grid>
                )}
              </Column>
              <hr />
              <div>
                <Column lg={16}>
                  <DataTable
                    rows={patientSearchResults}
                    headers={patientSearchHeaderData}
                    isSortable
                  >
                    {({ rows, headers, getHeaderProps, getTableProps }) => (
                      <TableContainer title="Patient Results">
                        <Table {...getTableProps()}>
                          <TableHead>
                            <TableRow>
                              <TableHeader></TableHeader>
                              {headers.map((header) => (
                                <TableHeader
                                  key={header.key}
                                  {...getHeaderProps({ header })}
                                >
                                  {header.header}
                                </TableHeader>
                              ))}
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            <>
                              {rows
                                .slice((page - 1) * pageSize)
                                .slice(0, pageSize)
                                .map((row) => (
                                  <TableRow key={row.id}>
                                    <TableCell>
                                      {" "}
                                      <RadioButton
                                        name="radio-group"
                                        onClick={patientSelected}
                                        labelText=""
                                        id={row.id}
                                      />
                                    </TableCell>
                                    {row.cells.map((cell) => (
                                      <TableCell key={cell.id}>
                                        {cell.value}
                                      </TableCell>
                                    ))}
                                  </TableRow>
                                ))}
                            </>
                          </TableBody>
                        </Table>
                      </TableContainer>
                    )}
                  </DataTable>
                  <Pagination
                    onChange={handlePageChange}
                    page={page}
                    pageSize={pageSize}
                    pageSizes={[5, 10, 20, 30]}
                    totalItems={patientSearchResults.length}
                  ></Pagination>
                </Column>
              </div>
              <div className="formInlineDiv">
                <div className="searchActionButtons">
                  <Button type="button" onClick={handleSearchReferralPatient}>
                    <FormattedMessage id="referral.main.button" />
                  </Button>
                </div>
              </div>
            </Form>
          )}
        </Formik>
      </div>
    </>
  );
}

export default injectIntl(ReferredOutTests);
