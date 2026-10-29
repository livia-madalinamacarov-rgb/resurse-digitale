package com.universitate.resurse.controller;
import com.universitate.resurse.model.*;
import com.universitate.resurse.repository.*;
import org.springframework.ui.Model;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.*;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private MaterialRepository materialRepository ;
    private final StudentResourceRepository studentResourceRepository;
    private final HomeworkRepository homeworkRepository;
    private final ResourceRequestRepository requestRepository;
    private final SubscriptionCredentialRepository credentialRepository;
    private final ProfessorRequestRepository professorRequestRepository;
    private final ProfessorResourceRepository professorResourceRepository;
    private final LogRepository logRepository;
    private final ActivityUsageRepository activityUsageRepository;

    public PageController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          CourseRepository courseRepository,
                          EnrollmentRepository enrollmentRepository,
                          MaterialRepository materialRepository, StudentResourceRepository studentResourceRepository, HomeworkRepository homeworkRepository, ResourceRequestRepository requestRepository, SubscriptionCredentialRepository credentialRepository, ProfessorRequestRepository professorRequestRepository, ProfessorResourceRepository professorResourceRepository, LogRepository logRepository, ActivityUsageRepository activityUsageRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.materialRepository = materialRepository;
        this.studentResourceRepository = studentResourceRepository;

        this.homeworkRepository = homeworkRepository;
        this.requestRepository = requestRepository;
        this.credentialRepository = credentialRepository;
        this.professorRequestRepository = professorRequestRepository;
        this.professorResourceRepository = professorResourceRepository;
        this.logRepository = logRepository;
        this.activityUsageRepository = activityUsageRepository;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        user.setRole("STUDENT");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Email sau parola gresita!");
        }


        return "login";
    }


    @GetMapping("/student")
    public String studentPage(HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Enrollment> enrollments =
                enrollmentRepository.findByStudentId(user.getId());

        List<Long> enrolledCourseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toList());

        List<Course> enrolledCourses =
                courseRepository.findAllById(enrolledCourseIds);

        List<Course> availableCourses = courseRepository.findAll()
                .stream()
                .filter(course -> !enrolledCourseIds.contains(course.getId()))
                .collect(Collectors.toList());

        List<StudentResource> resources =
                studentResourceRepository.findByStudentId(user.getId());

        List<ActivityUsage> usages =
                activityUsageRepository.findByStudentId(user.getId());

        int allocatedTokens = resources.stream()
                .mapToInt(StudentResource::getAllocatedTokens)
                .sum();

        int remainingTokens = resources.stream()
                .mapToInt(StudentResource::getRemainingTokens)
                .sum();

        int usedTokens = usages.stream()
                .mapToInt(ActivityUsage::getTokensUsed)
                .sum();

        int allocatedSubscriptions = resources.stream()
                .mapToInt(StudentResource::getAllocatedSubscriptions)
                .sum();

        int usedSubscriptions = resources.stream()
                .mapToInt(StudentResource::getUsedSubscriptions)
                .sum();

        model.addAttribute("availableCourses", availableCourses);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("allocatedTokens", allocatedTokens);
        model.addAttribute("remainingTokens", remainingTokens);
        model.addAttribute("usedTokens", usedTokens);
        model.addAttribute("allocatedSubscriptions", allocatedSubscriptions);
        model.addAttribute("usedSubscriptions", usedSubscriptions);

        return "student";
    }


    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session) {
        User user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElse(null);

        if (user == null) {
            return "redirect:/login?error=true";
        }
        session.setAttribute("user", user);
        logAction(user, "LOGIN", "Utilizator autentificat");

        if (user.getRole().equals("ADMIN")) {
            return "redirect:/admin";
        }

        if (user.getRole().equals("PROFESOR")) {
            return "redirect:/profesor";
        }

        if (user.getRole().equals("STUDENT")) {
            return "redirect:/student";
        }

        if (user.getRole().equals("AUDIT")) {
            return "redirect:/audit";
        }

        return "redirect:/home";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return "redirect:/reset-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "redirect:/login";
    }

    @GetMapping("/admin")
    public String adminPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("profRequests", professorRequestRepository.findAll());
        model.addAttribute("adminStudentRequests", requestRepository.findByNeedsAdminApprovalTrueAndApprovedFalse());


        return "admin";
    }


    @PostMapping("/admin/update-role")
    public String updateRole(@RequestParam Long id,
                             @RequestParam String role,
                             HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/login";
        }

        if (!List.of("ADMIN", "PROFESOR", "STUDENT", "AUDIT")
                .contains(role)) {
            return "redirect:/admin";
        }

        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            if (user.getId().equals(admin.getId())
                    && !"ADMIN".equals(role)) {
                return "redirect:/admin";
            }

            user.setRole(role);
            userRepository.save(user);

            logAction(admin, "UPDATE_ROLE",
                    "Rol modificat pentru utilizator ID: " + id);
        }

        return "redirect:/admin";
    }


    @Transactional
    @PostMapping("/student/enroll")
    public String enrollStudent(@RequestParam Long courseId,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String year,
                                @RequestParam String group,
                                @RequestParam String specialization,
                                HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        boolean exists = enrollmentRepository
                .existsByStudentIdAndCourseId(user.getId(), courseId);

        if (exists) {
            session.setAttribute("studentErrorMessage",
                    "Esti deja inscris la acest curs!");
            return "redirect:/student";
        }

        Course course = courseRepository.findById(courseId).orElse(null);

        if (course == null) {
            session.setAttribute("studentErrorMessage",
                    "Cursul nu exista!");
            return "redirect:/student";
        }

        int enrolledCount = enrollmentRepository
                .findByCourseId(courseId)
                .size();

        if (enrolledCount >= course.getMaxStudents()) {
            session.setAttribute("studentErrorMessage",
                    "Numarul maxim de studenti a fost atins!");
            return "redirect:/student";
        }

        StudentResource resource = studentResourceRepository
                .findByStudentIdAndCourseId(user.getId(), courseId)
                .orElse(null);

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(user.getId());
        enrollment.setCourseId(courseId);

        enrollmentRepository.save(enrollment);

        if (resource == null) {
            resource = new StudentResource();

            resource.setStudentId(user.getId());
            resource.setCourseId(courseId);

            resource.setAllocatedTokens(course.getTokensPerStudent());
            resource.setRemainingTokens(course.getTokensPerStudent());

            resource.setAllocatedSubscriptions(
                    course.getSubscriptionsPerStudent());
            resource.setRemainingSubscriptions(
                    course.getSubscriptionsPerStudent());

            resource.setUsedSubscriptions(0);
            resource.setSubscriptionValidated(false);

            studentResourceRepository.save(resource);
        }

        session.setAttribute("successMessage",
                "Te-ai inscris cu succes la curs!");

        return "redirect:/student";
    }

    @GetMapping("/student/enroll-form")
    public String enrollForm(@RequestParam Long courseId,
                             HttpSession session,
                             Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("STUDENT")) {
            return "redirect:/login";
        }

        model.addAttribute("courseId", courseId);

        return "enroll";
    }

    @PostMapping("/student/unenroll")
    public String unenrollStudent(
            @RequestParam Long courseId,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        boolean enrolled = enrollmentRepository
                .existsByStudentIdAndCourseId(user.getId(), courseId);

        if (!enrolled) {
            return "redirect:/student";
        }

        enrollmentRepository.deleteByStudentIdAndCourseId(
                user.getId(), courseId);

        session.setAttribute("successMessage",
                "Te-ai dezinscris cu succes de la curs!");

        return "redirect:/student";
    }

    @GetMapping("/profesor")
    public String profesorPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("PROFESOR")) {
            return "redirect:/login";
        }
        ProfessorResource res = professorResourceRepository
                .findByProfessorId(user.getId())
                .orElse(null);

        model.addAttribute("profResource", res);

        List<Course> courses = courseRepository.findByProfessorId(user.getId());

        List<Long> courseIds = courses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        List<Homework> homeworks = homeworkRepository.findByCourseIdIn(courseIds);

        model.addAttribute("courses", courses);
        model.addAttribute("homeworks", homeworks);

        model.addAttribute("requests",
                requestRepository.findByApprovedFalse()
                        .stream()
                        .filter(r -> !r.isNeedsAdminApproval())
                        .filter(r -> courseIds.contains(r.getCourseId()))
                        .collect(Collectors.toList())
        );

        return "profesor";
    }

    @GetMapping("/audit")
    public String auditPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("AUDIT")) {
            return "redirect:/login";
        }

        model.addAttribute("logs", logRepository.findAll());

        return "audit";
    }

    @GetMapping("/student/course/{courseId}")
    public String coursePage(@PathVariable Long courseId,
                             HttpSession session,
                             Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("STUDENT")) {
            return "redirect:/login";
        }

        boolean enrolled = enrollmentRepository
                .existsByStudentIdAndCourseId(user.getId(), courseId);

        if (!enrolled) {
            return "redirect:/student";
        }

        Course course = courseRepository.findById(courseId).orElse(null);

        if (course == null) {
            return "redirect:/student";
        }

        model.addAttribute("course", course);
        model.addAttribute("materials", materialRepository.findByCourseId(courseId));

        StudentResource resources = studentResourceRepository
                .findByStudentIdAndCourseId(user.getId(), courseId)
                .orElse(null);

        model.addAttribute("resources", resources);
        model.addAttribute("credentials",
                credentialRepository.findByStudentId(user.getId()));

        model.addAttribute("successMessage", session.getAttribute("successMessage"));
        model.addAttribute("studentErrorMessage", session.getAttribute("studentErrorMessage"));

        session.removeAttribute("successMessage");
        session.removeAttribute("studentErrorMessage");
        session.removeAttribute("errorMessage");
        return "course";
    }

    @PostMapping("/profesor/upload-material")
    public String uploadMaterial(@RequestParam Long courseId,
                                 @RequestParam String title,
                                 @RequestParam MultipartFile file,
                                 HttpSession session) throws IOException {

        User user = (User) session.getAttribute("user");

        // 🔒 verificare login + rol
        if (user == null || !user.getRole().equals("PROFESOR")) {
            return "redirect:/login";
        }

        // 🔒 verificare cursul aparține profesorului
        Course course = courseRepository.findById(courseId).orElse(null);

        if (course == null || !course.getProfessorId().equals(user.getId())) {
            return "redirect:/profesor";
        }

        // 🔥 salvare fișier
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        String uploadPath = System.getProperty("user.dir") + "/uploads/";

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File destination = new File(uploadPath + fileName);
        file.transferTo(destination);

        // 🔥 salvare în DB
        Material material = new Material();
        material.setCourseId(courseId);
        material.setTitle(title);
        material.setFileName(fileName);

        materialRepository.save(material);
        logAction(user, "UPLOAD_MATERIAL",
                "Material incarcat la curs ID: " + courseId +
                        ", titlu: " + title);

        return "redirect:/profesor";
    }
    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> viewFile(@PathVariable String fileName) {
        String filePath = System.getProperty("user.dir") + "/uploads/" + fileName;

        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @Transactional
    @PostMapping("/student/use-tokens")
    public String useTokens(@RequestParam Long courseId,
                            @RequestParam String activity,
                            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        String redirect = "redirect:/student/course/" + courseId;

        // Verificam daca studentul este inscris la curs
        if (!enrollmentRepository.existsByStudentIdAndCourseId(
                user.getId(), courseId)) {
            return "redirect:/student";
        }

        Map<String, Integer> costs = Map.of(
                "summary", 10,
                "image", 50,
                "code", 5000,
                "translate", 20,
                "chat", 15,
                "analysis", 30,
                "presentation", 100,
                "quiz", 25,
                "debug", 40
        );

        // Activitatea essay are un cost separat
        int cost = "essay".equals(activity)
                ? 60
                : costs.getOrDefault(activity, -1);

        if (cost < 0) {
            session.setAttribute("studentErrorMessage",
                    "Activitate necunoscuta!");
            return redirect;
        }

        StudentResource resource = studentResourceRepository
                .findByStudentIdAndCourseId(user.getId(), courseId)
                .orElse(null);

        if (resource == null) {
            session.setAttribute("studentErrorMessage",
                    "Nu ai resurse alocate pentru acest curs!");
            return redirect;
        }

        if (resource.getRemainingTokens() < cost) {
            session.setAttribute("studentErrorMessage",
                    "Nu ai suficiente token-uri!");
            return redirect;
        }

        resource.setRemainingTokens(
                resource.getRemainingTokens() - cost
        );

        studentResourceRepository.save(resource);

        ActivityUsage usage = new ActivityUsage();
        usage.setStudentId(user.getId());
        usage.setCourseId(courseId);
        usage.setActivity(activity);
        usage.setTokensUsed(cost);

        activityUsageRepository.save(usage);

        logAction(user, "USE_TOKENS",
                "Activitate: " + activity +
                        ", curs ID: " + courseId);

        session.setAttribute("successMessage",
                "Ai utilizat " + cost + " token-uri!");

        return redirect;
    }


    @PostMapping("/student/use-subscription")
    public String useSubscription(@RequestParam Long courseId,
                                  HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("STUDENT")) {
            return "redirect:/login";
        }

        StudentResource resource = studentResourceRepository
                .findByStudentIdAndCourseId(user.getId(), courseId)
                .orElse(null);

        if (resource == null) {
            return "redirect:/student/course/" + courseId;
        }


        if (resource.getRemainingSubscriptions() > 0) {
            resource.setRemainingSubscriptions(resource.getRemainingSubscriptions() - 1);
            resource.setUsedSubscriptions(resource.getUsedSubscriptions() + 1);
            studentResourceRepository.save(resource);
        }

        return "redirect:/student/course/" + courseId;
    }

    @PostMapping("/student/upload-homework")
    public String uploadHomework(@RequestParam Long courseId,
                                 @RequestParam MultipartFile file,
                                 HttpSession session) throws IOException {

        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("STUDENT")) {
            return "redirect:/login";
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        String uploadPath = System.getProperty("user.dir") + "/uploads/homework/";

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File destination = new File(uploadPath + fileName);
        file.transferTo(destination);
        Homework homework = new Homework();
        homework.setStudentId(user.getId());
        homework.setCourseId(courseId);
        homework.setFileName(fileName);

        homeworkRepository.save(homework);
        logAction(user, "UPLOAD_HOMEWORK",
                "Tema incarcata la curs ID: " + courseId +
                        ", fisier: " + fileName);

        return "redirect:/student/course/" + courseId;
    }
    @GetMapping("/homework/{fileName:.+}")
    public ResponseEntity<Resource> viewHomework(@PathVariable String fileName) {
        String filePath = System.getProperty("user.dir") + "/uploads/homework/" + fileName;

        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
    @PostMapping("/student/request-resources")
    public String requestResources(@RequestParam Long courseId,
                                   @RequestParam(defaultValue = "0") int tokens,
                                   @RequestParam(defaultValue = "0") int helpRequests,
                                   HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("STUDENT")) {
            return "redirect:/login";
        }

        if (!enrollmentRepository.existsByStudentIdAndCourseId(user.getId(), courseId)) {
            return "redirect:/student";
        }

        if (tokens < 0) tokens = 0;
        if (helpRequests < 0) helpRequests = 0;
        if (helpRequests > 2) helpRequests = 2;

        if (tokens == 0 && helpRequests == 0) {
            return "redirect:/student/course/" + courseId;
        }

        ResourceRequest r = new ResourceRequest();
        r.setStudentId(user.getId());
        r.setCourseId(courseId);
        r.setRequestedTokens(tokens);
        r.setRequestedHelpRequests(helpRequests);

        requestRepository.save(r);
        logAction(user, "REQUEST_RESOURCES",
                "Token-uri cerute: " + tokens +
                        ", cereri ajutor cerute: " + helpRequests +
                        ", curs ID: " + courseId);

        return "redirect:/student/course/" + courseId;
    }

    @Transactional
    @PostMapping("/profesor/approve-request")
    public String approve(@RequestParam Long requestId,
                          HttpSession session) {

        User professor = (User) session.getAttribute("user");

        // 1. Verificam daca utilizatorul este profesor
        if (professor == null ||
                !"PROFESOR".equals(professor.getRole())) {
            return "redirect:/login";
        }

        // 2. Cautam cererea studentului
        ResourceRequest request = requestRepository
                .findById(requestId)
                .orElse(null);

        if (request == null || request.isApproved() || request.isNeedsAdminApproval()) {
            return "redirect:/profesor";
        }

        // 3. Verificam daca profesorul detine cursul
        Course course = courseRepository
                .findById(request.getCourseId())
                .orElse(null);

        if (course == null ||
                !professor.getId().equals(course.getProfessorId())) {

            session.setAttribute("profesorErrorMessage",
                    "Nu poti aproba cereri pentru alte cursuri!");

            return "redirect:/profesor";
        }

        if (!enrollmentRepository.existsByStudentIdAndCourseId(
                request.getStudentId(), request.getCourseId())) {
            session.setAttribute("profesorErrorMessage",
                    "Studentul nu mai este inscris la curs.");
            return "redirect:/profesor";
        }

        // 4. Verificam resursele profesorului
        ProfessorResource professorResource =
                professorResourceRepository
                        .findByProfessorId(professor.getId())
                        .orElse(null);

        int requestedTokens = request.getRequestedTokens();

        if (requestedTokens < 0 || request.getRequestedHelpRequests() < 0 || request.getRequestedHelpRequests() > 2) {
            session.setAttribute("profesorErrorMessage",
                    "Numar invalid de token-uri!");
            return "redirect:/profesor";
        }

        // 5. Daca nu are suficiente resurse,
        // trimitem cererea administratorului
        if (professorResource == null ||
                professorResource.getTokens() < requestedTokens) {

            request.setNeedsAdminApproval(true);
            requestRepository.save(request);

            session.setAttribute("profesorErrorMessage",
                    "Nu ai suficiente token-uri. " +
                            "Cererea a fost trimisa administratorului.");

            return "redirect:/profesor";
        }

        // 6. Cautam resursele studentului
        StudentResource studentResource =
                studentResourceRepository
                        .findByStudentIdAndCourseId(
                                request.getStudentId(),
                                request.getCourseId())
                        .orElse(null);

        if (studentResource == null) {
            session.setAttribute("profesorErrorMessage",
                    "Studentul nu are resurse pentru acest curs!");
            return "redirect:/profesor";
        }

        // 7. Alocam resursele studentului
        studentResource.setRemainingTokens(
                studentResource.getRemainingTokens()
                        + requestedTokens);

        studentResource.setAllocatedTokens(
                studentResource.getAllocatedTokens() + requestedTokens
        );

        int help = Math.max(0,
                Math.min(2, request.getRequestedHelpRequests()));

        studentResource.setRemainingSubscriptions(
                studentResource.getRemainingSubscriptions() + help);

        studentResource.setAllocatedSubscriptions(
                studentResource.getAllocatedSubscriptions() + help
        );

        studentResourceRepository.save(studentResource);

        // 8. Scadem token-urile de la profesor
        professorResource.setTokens(
                professorResource.getTokens() - requestedTokens);

        professorResourceRepository.save(professorResource);

        // 9. Marcam cererea ca aprobata
        request.setApproved(true);
        request.setNeedsAdminApproval(false);
        requestRepository.save(request);

        // 10. Inregistram actiunea
        logAction(professor, "APPROVE_STUDENT_REQUEST",
                "Cerere aprobata, ID: " + requestId);

        session.setAttribute("successMessage",
                "Cererea a fost aprobata cu succes!");

        return "redirect:/profesor";
    }

    @PostMapping("/admin/add-tokens")
    public String addTokens(@RequestParam Long studentId,
                            @RequestParam Long courseId,
                            @RequestParam int tokens,
                            HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null || !admin.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        if (tokens <= 0) {
            session.setAttribute("adminErrorMessage",
                    "Numarul de token-uri trebuie sa fie pozitiv!");
            return "redirect:/admin";
        }

        StudentResource resource = studentResourceRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);

        if (resource != null) {
            resource.setRemainingTokens(resource.getRemainingTokens() + tokens);

            resource.setAllocatedTokens(
                    resource.getAllocatedTokens() + tokens
            );
            studentResourceRepository.save(resource);
            logAction(admin, "ADD_TOKENS",
                    "Admin a adaugat " + tokens +
                            " token-uri studentului ID: " + studentId +
                            ", curs ID: " + courseId);
        }

        return "redirect:/admin";
    }

    @PostMapping("/admin/send-credentials")
    public String sendCredentials(@RequestParam Long studentId,
                                  @RequestParam Long courseId,
                                  @RequestParam String username,
                                  @RequestParam String password,
                                  HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null || !admin.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        SubscriptionCredential c = new SubscriptionCredential();
        c.setStudentId(studentId);
        c.setCourseId(courseId);
        c.setUsername(username);
        c.setPassword(password);

        credentialRepository.save(c);
        logAction(admin, "SEND_CREDENTIALS",
                "Credentiale trimise studentului ID: " + studentId +
                        ", curs ID: " + courseId);

        return "redirect:/admin";
    }

    @PostMapping("/admin/assign-professor-tokens")
    public String assignTokensToProfessor(Long professorId, int tokens) {

        // simplu: salvezi într-o variabilă sau tabel (dacă vrei extra)

        System.out.println("Profesor " + professorId + " a primit " + tokens + " token-uri");

        return "redirect:/admin";
    }
    @PostMapping("/profesor/request-resources")
    public String professorRequestResources(@RequestParam int tokens,
                                            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("PROFESOR")) {
            return "redirect:/login";
        }

        ProfessorRequest request = new ProfessorRequest();
        request.setProfessorId(user.getId());
        request.setTokensRequested(tokens);

        professorRequestRepository.save(request);

        return "redirect:/profesor";
    }
    @PostMapping("/admin/approve-prof-request")
    public String approveProfRequest(@RequestParam Long id,
                                     HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null || !admin.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        ProfessorRequest request = professorRequestRepository.findById(id).orElse(null);

        if (request != null && !request.isApproved()) {

            request.setApproved(true);
            professorRequestRepository.save(request);

            int requested = request.getTokensRequested();
            int approvedTokens;

            if (requested > 10000) {
                approvedTokens = requested / 10;
            } else {
                approvedTokens = requested;
            }

            ProfessorResource res = professorResourceRepository
                    .findByProfessorId(request.getProfessorId())
                    .orElse(null);

            if (res == null) {
                res = new ProfessorResource();
                res.setProfessorId(request.getProfessorId());
                res.setTokens(0);
            }

            res.setTokens(res.getTokens() + approvedTokens);
            professorResourceRepository.save(res);

            logAction(admin, "APPROVE_PROFESSOR_REQUEST",
                    "Cerere profesor aprobata, profesor ID: " +
                            request.getProfessorId() +
                            ", token-uri aprobate: " + approvedTokens);
        }

        return "redirect:/admin";
    }

    @Transactional
    @PostMapping("/admin/approve-student-request")
    public String approveStudentRequestByAdmin(
            @RequestParam Long requestId,
            HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/login";
        }

        ResourceRequest r = requestRepository
                .findById(requestId)
                .orElse(null);

        if (r == null || r.isApproved()
                || !r.isNeedsAdminApproval()) {
            return "redirect:/admin";
        }

        StudentResource studentRes = studentResourceRepository
                .findByStudentIdAndCourseId(
                        r.getStudentId(), r.getCourseId())
                .orElse(null);

        if (studentRes == null) {
            session.setAttribute("adminErrorMessage",
                    "Resursele studentului nu au fost gasite!");
            return "redirect:/admin";
        }

        if (!enrollmentRepository.existsByStudentIdAndCourseId(r.getStudentId(), r.getCourseId())) {
            session.setAttribute("adminErrorMessage", "Studentul nu mai este inscris la curs.");
            return "redirect:/admin";
        }

        int tokens = r.getRequestedTokens();
        int help = r.getRequestedHelpRequests();

        if (tokens < 0 || help < 0 || help > 2) {
            session.setAttribute("adminErrorMessage",
                    "Cererea contine valori invalide!");
            return "redirect:/admin";
        }

        studentRes.setRemainingTokens(
                studentRes.getRemainingTokens() + tokens);

        studentRes.setAllocatedTokens(
                studentRes.getAllocatedTokens() + tokens);

        studentRes.setRemainingSubscriptions(
                studentRes.getRemainingSubscriptions() + help);

        studentRes.setAllocatedSubscriptions(
                studentRes.getAllocatedSubscriptions() + help);

        studentResourceRepository.save(studentRes);

        r.setApproved(true);
        r.setNeedsAdminApproval(false);
        requestRepository.save(r);

        logAction(admin, "ADMIN_APPROVE_STUDENT_REQUEST",
                "Cerere aprobata de admin, ID: " + requestId);

        session.setAttribute("successMessage",
                "Cererea a fost aprobata cu succes!");

        return "redirect:/admin";
    }

    @PostMapping("/student/validate-subscription")
    public String validateSubscription(
            @RequestParam Long courseId,
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        if (!enrollmentRepository.existsByStudentIdAndCourseId(
                user.getId(), courseId)) {
            return "redirect:/student";
        }

        SubscriptionCredential credential =
                credentialRepository.findByStudentId(user.getId())
                        .stream()
                        .filter(c -> courseId.equals(c.getCourseId()))
                        .filter(c -> username.equals(c.getUsername()))
                        .filter(c -> password.equals(c.getPassword()))
                        .findFirst()
                        .orElse(null);

        if (credential == null) {
            session.setAttribute("studentErrorMessage",
                    "Datele abonamentului sunt incorecte!");
            return "redirect:/student/course/" + courseId;
        }

        StudentResource resource = studentResourceRepository
                .findByStudentIdAndCourseId(user.getId(), courseId)
                .orElse(null);

        if (resource == null) {
            session.setAttribute("studentErrorMessage",
                    "Nu exista resurse pentru acest curs!");
            return "redirect:/student/course/" + courseId;
        }

        resource.setSubscriptionValidated(true);
        studentResourceRepository.save(resource);

        logAction(user, "VALIDATE_SUBSCRIPTION",
                "Abonament validat pentru curs ID: " + courseId);

        session.setAttribute("successMessage",
                "Abonamentul a fost validat cu succes!");

        return "redirect:/student/course/" + courseId;
    }

    @PostMapping("/profesor/create-course")
    public String createCourse(@RequestParam String name,
                               @RequestParam String description,
                               @RequestParam int maxStudents,
                               @RequestParam int tokensPerStudent,
                               @RequestParam int subscriptionsPerStudent,
                               HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("PROFESOR")) {
            return "redirect:/login";
        }

        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setProfessorId(user.getId());

        // 🔥 IMPORTANT – trebuie să ai câmpuri în entity
        course.setMaxStudents(maxStudents);
        course.setTokensPerStudent(tokensPerStudent);
        course.setSubscriptionsPerStudent(subscriptionsPerStudent);

        courseRepository.save(course);

        return "redirect:/profesor";
    }

    private void logAction(User user, String action, String details) {
        if (user == null) {
            return;
        }

        LogEntry log = new LogEntry();
        log.setUserId(user.getId());
        log.setUserRole(user.getRole());
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(java.time.LocalDateTime.now());

        logRepository.save(log);
    }
    @GetMapping("/admin/student-stats")
    public String studentStats(@RequestParam Long studentId,
                               Model model,
                               HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null || !admin.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }
        List<StudentResource> resources = studentResourceRepository.findByStudentId(studentId);
        List<ActivityUsage> usages = activityUsageRepository.findByStudentId(studentId);
        int allocatedTokens = resources.stream()
                .mapToInt(StudentResource::getAllocatedTokens)
                .sum();

        int remainingTokens = resources.stream()
                .mapToInt(StudentResource::getRemainingTokens)
                .sum();

        int usedTokens = usages.stream()
                .mapToInt(ActivityUsage::getTokensUsed)
                .sum();

        int allocatedSubscriptions = resources.stream()
                .mapToInt(StudentResource::getAllocatedSubscriptions)
                .sum();

        int usedSubscriptions = resources.stream()
                .mapToInt(StudentResource::getUsedSubscriptions)
                .sum();


        model.addAttribute("studentId", studentId);
        model.addAttribute("resources", resources);

        model.addAttribute("allocatedTokens", allocatedTokens);
        model.addAttribute("remainingTokens", remainingTokens);
        model.addAttribute("usedTokens", usedTokens);

        model.addAttribute("allocatedSubscriptions", allocatedSubscriptions);
        model.addAttribute("usedSubscriptions", usedSubscriptions);
        model.addAttribute("usages", usages);
        return "student-stats";
    }
    @GetMapping("/admin/course-stats")
    public String courseStats(@RequestParam Long courseId,
                              Model model,
                              HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null || !admin.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        List<StudentResource> resources =
                studentResourceRepository.findByCourseId(courseId);
        int allocatedTokens = resources.stream().mapToInt(StudentResource::getAllocatedTokens).sum();
        int remainingTokens = resources.stream().mapToInt(StudentResource::getRemainingTokens).sum();

        int usedTokens = activityUsageRepository
                .findByCourseId(courseId)
                .stream()
                .mapToInt(ActivityUsage::getTokensUsed)
                .sum();

        int allocatedSubscriptions = resources.stream().mapToInt(StudentResource::getAllocatedSubscriptions).sum();
        int usedSubscriptions = resources.stream().mapToInt(StudentResource::getUsedSubscriptions).sum();

        List<ActivityUsage> usages = activityUsageRepository.findByCourseId(courseId);

        int studentCount = resources.size();

        int totalTokens = resources.stream()
                .mapToInt(StudentResource::getRemainingTokens)
                .sum();

        int totalHelp = resources.stream()
                .mapToInt(StudentResource::getRemainingSubscriptions)
                .sum();

        double avgTokens = studentCount > 0 ? (double) totalTokens / studentCount : 0;
        double avgHelp = studentCount > 0 ? (double) totalHelp / studentCount : 0;

        model.addAttribute("courseId", courseId);
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("totalTokens", totalTokens);
        model.addAttribute("totalHelp", totalHelp);
        model.addAttribute("avgTokens", avgTokens);
        model.addAttribute("avgHelp", avgHelp);
        model.addAttribute("usages", usages);

        return "course-stats";
    }
    @GetMapping("/admin/university-stats")
    public String universityStats(Model model,
                                  HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null || !admin.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        List<StudentResource> resources = studentResourceRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<User> users = userRepository.findAll();

        long totalStudents = users.stream()
                .filter(u -> u.getRole().equals("STUDENT"))
                .count();

        int totalCourses = courses.size();

        int totalTokensRemaining = resources.stream()
                .mapToInt(StudentResource::getRemainingTokens)
                .sum();

        int totalHelpRemaining = resources.stream()
                .mapToInt(StudentResource::getRemainingSubscriptions)
                .sum();

        int totalEnrollments = resources.size();

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalEnrollments", totalEnrollments);
        model.addAttribute("totalTokensRemaining", totalTokensRemaining);
        model.addAttribute("totalHelpRemaining", totalHelpRemaining);

        return "university-stats";
    }
}