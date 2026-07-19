package com.crm.controller;

import com.crm.model.Contact;
import com.crm.model.CrmDataSnapshot;
import com.crm.model.UserAccount;
import com.crm.repository.ExportOwner;
import com.crm.repository.ImportedWorkspace;
import com.crm.repository.LocalUserRepository;
import com.crm.repository.UserRepository;
import com.crm.service.CrmWorkspaceService;
import com.crm.service.DialogService;
import com.crm.service.ThemeService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;
import org.fxmisc.richtext.CodeArea;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * FXML composition root for the main window.
 *
 * <p>Feature behavior is delegated to focused controllers; this class only wires
 * injected controls, coordinates the shared workspace snapshot, and exposes the
 * event methods referenced by MainView.fxml.</p>
 */
public final class MainController {
    private static final double LEFT_SIDEBAR_DEFAULT_WIDTH = 216;
    private static final double LEFT_SIDEBAR_MIN_WIDTH = 176;
    private static final double LEFT_SIDEBAR_MAX_WIDTH = 360;
    private static final double RIGHT_SIDEBAR_DEFAULT_WIDTH = 296;
    private static final double RIGHT_SIDEBAR_MIN_WIDTH = 260;
    private static final double RIGHT_SIDEBAR_MAX_WIDTH = 440;
    private static final double SIDEBAR_COLLAPSE_THRESHOLD = 52;
    private static final double MIN_WORKSPACE_WIDTH = 520;
    private static final double RESIZE_HANDLE_WIDTH = 6;

    @FXML private BorderPane appShell;
    @FXML private HBox leftSidebarWrapper;
    @FXML private HBox rightSidebarWrapper;
    @FXML private VBox leftPanel;
    @FXML private TableView<Contact> contactsTable;
    @FXML private TableColumn<Contact, String> nameColumn;
    @FXML private TableColumn<Contact, String> companyColumn;
    @FXML private TableColumn<Contact, String> titleColumn;
    @FXML private TableColumn<Contact, String> emailColumn;
    @FXML private TableColumn<Contact, String> phoneColumn;
    @FXML private TableColumn<Contact, String> lastInteractionColumn;
    @FXML private TableColumn<Contact, String> tagsColumn;
    @FXML private TableColumn<Contact, String> descriptionColumn;
    @FXML private TableColumn<Contact, Boolean> selectColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> rowsPerPageCombo;
    @FXML private Pagination contactsPagination;
    @FXML private Label paginationInfoLabel;
    @FXML private Button selectContactsBtn;
    @FXML private MenuButton contactsSortMenu;
    @FXML private ToggleButton contactsInlineEditToggle;
    @FXML private Button addFieldBtn;

    @FXML private ScrollPane homeView;
    @FXML private ScrollPane dashboardView;
    @FXML private VBox contactsView;
    @FXML private VBox calendarView;
    @FXML private VBox tasksView;
    @FXML private VBox notesView;
    @FXML private VBox genericView;
    @FXML private Label genericTitle;
    @FXML private FontIcon genericIcon;
    @FXML private VBox sidebarContainer;
    @FXML private VBox rightPanel;
    @FXML private Region leftResizeHandle;
    @FXML private Region rightResizeHandle;
    @FXML private Button leftSidebarToggleButton;
    @FXML private Button agendaToggleButton;
    @FXML private Button themeToggleBtn;
    @FXML private FontIcon themeToggleIcon;
    @FXML private Label currentUserLabel;
    @FXML private Label saveStatusLabel;
    @FXML private Button accountMenuButton;
    @FXML private FontIcon defaultAvatarIcon;
    @FXML private ImageView avatarImage;

    @FXML private AnchorPane timeLabelsContainer;
    @FXML private AnchorPane calendarTimelineArea;
    @FXML private HBox calendarContentRow;
    @FXML private ScrollPane calendarScrollPane;
    @FXML private DatePicker calendarDatePicker;
    @FXML private ComboBox<String> viewModeCombo;
    @FXML private Label selectedPeriodLabel;
    @FXML private Label calendarZoomLabel;
    @FXML private Label miniMonthYearLabel;
    @FXML private GridPane miniCalendarGrid;
    @FXML private Label activitiesTitle;
    @FXML private VBox upcomingActivitiesList;

    @FXML private TextField taskSearchField;
    @FXML private ComboBox<String> taskFilterCombo;
    @FXML private VBox taskListContainer;
    @FXML private Label tasksEmptyLabel;
    @FXML private Label tasksTotalCountLabel;
    @FXML private Label tasksTodayCountLabel;
    @FXML private Label tasksUpcomingCountLabel;
    @FXML private Label tasksCompletedCountLabel;

    @FXML private VBox notesLibraryPane;
    @FXML private VBox noteEditorPane;
    @FXML private TextField notesSearchField;
    @FXML private TilePane notesGrid;
    @FXML private Label notesEmptyLabel;
    @FXML private Label notesCountLabel;
    @FXML private Button notesBackButton;
    @FXML private ScrollPane notesBreadcrumbScroll;
    @FXML private HBox notesBreadcrumbItems;
    @FXML private TextField noteTitleField;
    @FXML private Label noteFormatLabel;
    @FXML private ComboBox<NotesController.FolderOption> noteFolderCombo;
    @FXML private ComboBox<NotesController.TaskOption> noteTaskCombo;
    @FXML private MenuButton noteOpenTaskButton;
    @FXML private HBox markdownToolbar;
    @FXML private ComboBox<String> noteFontFamilyCombo;
    @FXML private ComboBox<Double> noteFontSizeCombo;
    @FXML private ComboBox<String> noteFontWeightCombo;
    @FXML private ToggleButton noteBoldToggle;
    @FXML private ToggleButton noteItalicToggle;
    @FXML private HBox notePreviewSettingsBar;
    @FXML private ComboBox<String> notePreviewFontFamilyCombo;
    @FXML private ComboBox<Double> notePreviewFontSizeCombo;
    @FXML private ColorPicker notePreviewColorPicker;
    @FXML private ToggleButton notePreviewToggle;
    @FXML private CodeArea noteContentArea;
    @FXML private ScrollPane notePreviewScroll;
    @FXML private VBox notePreviewContent;
    @FXML private Label noteEditorStatus;

    @FXML private Label homeGreetingLabel;
    @FXML private Label homeDateLabel;
    @FXML private Label homeContactsCountLabel;
    @FXML private Label homeTodayCountLabel;
    @FXML private Label homeWeekCountLabel;
    @FXML private Label homeNextTitleLabel;
    @FXML private Label homeNextTimeLabel;
    @FXML private VBox homeTodayList;
    @FXML private VBox homeUpcomingList;
    @FXML private VBox homeContactsList;
    @FXML private Label dashboardContactsCountLabel;
    @FXML private Label dashboardActivitiesCountLabel;
    @FXML private Label dashboardWeekCountLabel;
    @FXML private Label dashboardHoursCountLabel;
    @FXML private PieChart dashboardTagsChart;
    @FXML private BarChart<String, Number> dashboardActivityChart;
    @FXML private VBox dashboardInteractionsList;

    private final CrmWorkspaceService workspaceService = new CrmWorkspaceService();
    private final UserRepository userRepository = new LocalUserRepository();
    private ThemeService themeService;
    private DialogService dialogService;
    private ContactsController contactsController;
    private CalendarController calendarController;
    private TasksController tasksController;
    private NotesController notesController;
    private AccountController accountController;
    private OverviewController overviewController;
    private NavigationController navigationController;
    private Runnable logoutAction;
    private UserAccount currentUser;
    private boolean loadingWorkspace;
    private double leftExpandedWidth = LEFT_SIDEBAR_DEFAULT_WIDTH;
    private double rightExpandedWidth = RIGHT_SIDEBAR_DEFAULT_WIDTH;
    private boolean leftAutomaticallyCollapsed;
    private boolean rightAutomaticallyCollapsed;
    private boolean adjustingResponsiveSidebars;

    @FXML
    public void initialize() {
        themeService = new ThemeService(this::ownerWindow);
        dialogService = new DialogService(themeService);
        initializeResizableSidebars();
        navigationController = new NavigationController(homeView, dashboardView,
                contactsView, calendarView, tasksView, notesView, genericView,
                genericTitle, genericIcon, sidebarContainer);
        overviewController = new OverviewController(
                homeGreetingLabel, homeDateLabel,
                homeContactsCountLabel, homeTodayCountLabel, homeWeekCountLabel,
                homeNextTitleLabel, homeNextTimeLabel,
                homeTodayList, homeUpcomingList, homeContactsList,
                dashboardContactsCountLabel, dashboardActivitiesCountLabel,
                dashboardWeekCountLabel, dashboardHoursCountLabel,
                dashboardTagsChart, dashboardActivityChart, dashboardInteractionsList);
        contactsController = new ContactsController(contactsTable, nameColumn, companyColumn,
                titleColumn, emailColumn, phoneColumn, lastInteractionColumn, tagsColumn,
                descriptionColumn, selectColumn, searchField, rowsPerPageCombo, contactsPagination,
                paginationInfoLabel, selectContactsBtn, contactsSortMenu, contactsInlineEditToggle,
                addFieldBtn, themeService, this::handleDataChanged);
        calendarController = new CalendarController(calendarView, timeLabelsContainer,
                calendarTimelineArea, calendarContentRow, calendarScrollPane, calendarDatePicker,
                viewModeCombo, selectedPeriodLabel, calendarZoomLabel,
                miniMonthYearLabel, miniCalendarGrid, activitiesTitle, upcomingActivitiesList, themeService,
                dialogService, this::handleDataChanged, navigationController::showCalendar);
        notesController = new NotesController(notesLibraryPane, noteEditorPane, notesSearchField,
                notesGrid, notesEmptyLabel, notesCountLabel, noteTitleField, noteFormatLabel,
                notesBackButton, notesBreadcrumbScroll, notesBreadcrumbItems, noteFolderCombo,
                noteTaskCombo, noteOpenTaskButton, markdownToolbar, notePreviewToggle,
                noteFontFamilyCombo, noteFontSizeCombo, noteFontWeightCombo, noteBoldToggle, noteItalicToggle,
                notePreviewSettingsBar, notePreviewFontFamilyCombo, notePreviewFontSizeCombo, notePreviewColorPicker,
                noteContentArea, notePreviewScroll, notePreviewContent, noteEditorStatus,
                themeService, new NotesController.NoteActions() {
                    @Override public void dataChanged() { handleDataChanged(); }
                    @Override public void notePresentationChanged() { calendarController.refreshNoteLinks(); }
                    @Override public void showNotes() { navigationController.showNotes(); }
                    @Override public void openTask(LocalDate date, com.crm.model.Task task) {
                        calendarController.editTask(date, task);
                    }
                });
        calendarController.setNoteIntegration(new CalendarController.NoteIntegration() {
            @Override public List<com.crm.model.Note> notes() { return notesController.snapshot(); }
            @Override public List<com.crm.model.NoteFolder> folders() { return notesController.foldersSnapshot(); }
            @Override public List<com.crm.model.Note> notesForTask(String taskId) {
                return notesController.notesForTask(taskId);
            }
            @Override public void openNote(String noteId) { notesController.openById(noteId); }
        });
        tasksController = new TasksController(taskSearchField, taskFilterCombo, taskListContainer,
                tasksEmptyLabel, tasksTotalCountLabel, tasksTodayCountLabel,
                tasksUpcomingCountLabel, tasksCompletedCountLabel, themeService,
                new TasksController.TaskActions() {
                    @Override public void edit(LocalDate date, com.crm.model.Task task) {
                        calendarController.editTask(date, task);
                    }
                    @Override public void delete(LocalDate date, com.crm.model.Task task) {
                        calendarController.deleteTask(date, task);
                    }
                    @Override public void setCompleted(LocalDate date, com.crm.model.Task task, boolean completed) {
                        calendarController.setTaskCompleted(date, task, completed);
                    }
                    @Override public void openCalendar(LocalDate date) {
                        calendarController.showTaskInCalendar(date);
                    }
                    @Override public List<com.crm.model.Note> linkedNotes(String taskId) {
                        return notesController.notesForTask(taskId);
                    }
                    @Override public void openNote(String noteId) {
                        notesController.openById(noteId);
                    }
                });
        accountController = new AccountController(currentUserLabel, accountMenuButton,
                defaultAvatarIcon, avatarImage, themeService, dialogService);
        accountController.setDataTransferActions(this::exportData, this::importData);

        navigationController.initialize();
        contactsController.initialize();
        calendarController.initialize();
        tasksController.initialize();
        notesController.initialize();
        overviewController.refresh(List.of(), Map.of());
        updateThemeButton();
        themeToggleBtn.sceneProperty().addListener((observable, oldScene, newScene) ->
                themeService.applyTo(newScene));
    }

    public void setCurrentUser(UserAccount user, Runnable logoutAction) {
        setCurrentUser(user, logoutAction, null, 0);
    }

    public void setCurrentUser(UserAccount user, Runnable logoutAction,
                               BufferedImage preloadedAvatar, int preloadedPixelSize) {
        this.currentUser = user;
        this.logoutAction = logoutAction;
        themeService.restore(user.getPreferredTheme());
        themeService.applyTo(themeToggleBtn.getScene());
        updateThemeButton();
        calendarController.refreshTheme();
        notesController.refreshTheme();
        overviewController.setUser(user);
        refreshOverview();
        accountController.setCurrentUser(
                user, this::logout, preloadedAvatar, preloadedPixelSize);
        loadingWorkspace = true;
        setSaveStatus("Loading data…");
        workspaceService.openAsync(user).whenComplete((snapshot, failure) -> Platform.runLater(() -> {
            try {
                if (failure == null) applyUserData(snapshot);
                else {
                    LocalDate today = LocalDate.now();
                    applyUserData(new CrmDataSnapshot(new ArrayList<>(), new HashMap<>(), today, "Day", 1.0));
                    dialogService.showError("Local data cannot be read",
                            "Neither the data file nor its backup could be recovered. "
                                    + "The workspace will remain open without overwriting the file until you make a change.");
                }
            } finally {
                loadingWorkspace = false;
                setSaveStatus(failure == null ? "Data loaded" : "Data not loaded");
            }
        }));
    }

    public void requestInitialFocus() {
        Platform.runLater(homeView::requestFocus);
    }

    private void applyUserData(CrmDataSnapshot snapshot) {
        contactsController.setCustomFields(snapshot.contactCustomFields());
        contactsController.setQuickEditEnabled(snapshot.contactsQuickEdit());
        contactsController.setContacts(snapshot.contacts());
        calendarController.applyState(snapshot.tasksByDate(), snapshot.selectedDate(),
                snapshot.calendarViewMode(), snapshot.calendarZoom());
        notesController.applyState(snapshot.notes(), snapshot.noteFolders(), calendarController.tasksSnapshot());
        refreshOverview();
    }

    private void handleDataChanged() {
        refreshOverview();
        saveCurrentData();
    }

    private void refreshOverview() {
        Map<LocalDate, List<com.crm.model.Task>> tasks = calendarController.tasksSnapshot();
        overviewController.refresh(contactsController.snapshot(), tasks);
        tasksController.refresh(tasks);
        notesController.refreshTasks(tasks);
    }

    private void saveCurrentData() {
        if (loadingWorkspace || calendarController.selectedDate() == null) return;
        CrmDataSnapshot snapshot = CrmDataSnapshot.detachedCopyOf(contactsController.snapshot(),
                calendarController.tasksSnapshot(), notesController.snapshot(), notesController.foldersSnapshot(),
                calendarController.selectedDate(), calendarController.viewMode(), calendarController.zoom(),
                contactsController.customFieldsSnapshot(), contactsController.isQuickEditEnabled());
        workspaceService.requestSave(snapshot, state -> Platform.runLater(() -> handleSaveState(state)));
    }

    private Window ownerWindow() {
        Scene scene = themeToggleBtn == null ? null : themeToggleBtn.getScene();
        return scene == null ? null : scene.getWindow();
    }

    /** Writes the current account's workspace to a user-chosen desktop-compatible file. */
    private void exportData() {
        if (currentUser == null) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export data");
        chooser.setInitialFileName("VoidReach-CRM-" + LocalDate.now() + ".properties");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("VoidReach CRM data", "*.properties"));
        File selected = chooser.showSaveDialog(ownerWindow());
        if (selected == null) return;
        Path target = selected.toPath();
        setSaveStatus("Exporting…");
        CompletableFuture.runAsync(() -> workspaceService.exportCurrentUser(target)).whenComplete((ignored, failure) ->
                Platform.runLater(() -> {
                    if (failure != null) {
                        setSaveStatus("Export failed");
                        dialogService.showError("Export failed",
                                "The data could not be exported to the selected file.");
                    } else {
                        setSaveStatus("Data exported");
                        dialogService.showInfo("Export complete", "Data exported in desktop-compatible format.");
                    }
                }));
    }

    /** Loads a portable file, warning first when it belongs to a different account, then applies it. */
    private void importData() {
        if (currentUser == null) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import data");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("VoidReach CRM data", "*.properties"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
        File selected = chooser.showOpenDialog(ownerWindow());
        if (selected == null) return;
        Path source = selected.toPath();
        setSaveStatus("Importing…");
        CompletableFuture.supplyAsync(() -> workspaceService.readImport(source)).whenComplete((imported, failure) ->
                Platform.runLater(() -> {
                    if (failure != null) {
                        setSaveStatus("Import failed");
                        dialogService.showError("Import failed",
                                "The selected file could not be read as VoidReach CRM data.");
                        return;
                    }
                    ExportOwner owner = imported.owner();
                    if (owner != null && currentUser != null && !owner.email().equalsIgnoreCase(currentUser.getEmail())) {
                        String who = owner.name() == null || owner.name().isBlank()
                                ? owner.email() : owner.name() + " (" + owner.email() + ")";
                        boolean proceed = dialogService.confirmWarning("Data from another account",
                                "This file was exported by " + who + ", but you are signed in as "
                                        + currentUser.getEmail() + ".\n\nImporting it will replace your current "
                                        + "workspace with that account's data.",
                                "Import anyway");
                        if (!proceed) {
                            setSaveStatus("Import cancelled");
                            return;
                        }
                    }
                    applyImportedData(imported.snapshot());
                }));
    }

    private void applyImportedData(CrmDataSnapshot snapshot) {
        loadingWorkspace = true;
        applyUserData(snapshot);
        loadingWorkspace = false;
        setSaveStatus("Saving…");
        CompletableFuture.runAsync(() -> workspaceService.save(snapshot)).whenComplete((ignored, failure) ->
                Platform.runLater(() -> {
                    if (failure == null) {
                        setSaveStatus("Saved");
                        dialogService.showInfo("Import complete", "Desktop-compatible data imported.");
                    } else {
                        setSaveStatus("Save failed");
                        dialogService.showError("Data not saved", "The imported data could not be saved to disk.");
                    }
                }));
    }

    private void logout() {
        loadingWorkspace = true;
        setSaveStatus("Final save…");
        workspaceService.closeAsync().whenComplete((ignored, failure) -> Platform.runLater(() -> {
            loadingWorkspace = false;
            if (failure != null) dialogService.showError("Data not saved",
                    "The save could not be completed before signing out.");
            if (logoutAction != null) logoutAction.run();
        }));
    }

    private void handleSaveState(CrmWorkspaceService.SaveState state) {
        if (state == CrmWorkspaceService.SaveState.SAVING) setSaveStatus("Saving…");
        else if (state == CrmWorkspaceService.SaveState.SAVED) setSaveStatus("Saved");
        else {
            setSaveStatus("Save failed");
            dialogService.showError("Data not saved",
                    "The data could not be saved to disk. Your work remains open in this session so you can try again.");
        }
    }

    private void setSaveStatus(String status) {
        if (saveStatusLabel != null) saveStatusLabel.setText(status);
    }

    @FXML private void handleAccountMenu() { accountController.showMenu(); }
    @FXML private void handleNavigation(ActionEvent event) { navigationController.navigate(event); }
    @FXML private void handleAddContact() { contactsController.addContact(); }
    @FXML private void handleAddTask() { calendarController.createTask(LocalDate.now()); }
    @FXML private void handleAddNote() { notesController.createNote(); }
    @FXML private void handleAddNoteFolder() { notesController.createFolder(); }
    @FXML private void handleOpenNotesRoot() { notesController.navigateUp(); }
    @FXML private void handleCloseNote() { notesController.closeEditor(); }
    @FXML private void handleDeleteNote() { notesController.deleteCurrent(); }
    @FXML private void handleOpenLinkedTask() { notesController.openLinkedTask(); }
    @FXML private void handleToggleNotePreview() { notesController.togglePreview(); }
    @FXML private void handleMarkdownHeading() { notesController.markdownHeading(); }
    @FXML private void handleMarkdownBold() { notesController.markdownBold(); }
    @FXML private void handleMarkdownItalic() { notesController.markdownItalic(); }
    @FXML private void handleMarkdownLink() { notesController.markdownLink(); }
    @FXML private void handleMarkdownCode() { notesController.markdownCode(); }
    @FXML private void handleMarkdownChecklist() { notesController.markdownChecklist(); }
    @FXML private void handleResetNoteTypography() { notesController.resetTypography(); }
    @FXML private void handleResetPreviewTypography() { notesController.resetPreviewTypography(); }
    @FXML private void handleToggleContactSelection() { contactsController.toggleSelection(); }
    @FXML private void handleDeleteContact() { contactsController.deleteSelectedContacts(); }
    @FXML private void handleMiniPrevMonth() { calendarController.previousMiniMonth(); }
    @FXML private void handleMiniNextMonth() { calendarController.nextMiniMonth(); }
    @FXML private void handleToday() { calendarController.today(); }
    @FXML private void handlePrevDay() { calendarController.previousPeriod(); }
    @FXML private void handleNextDay() { calendarController.nextPeriod(); }
    @FXML private void handleCalendarZoomIn() { calendarController.zoomIn(); }
    @FXML private void handleCalendarZoomOut() { calendarController.zoomOut(); }
    @FXML private void handleCalendarZoomReset() { calendarController.resetZoom(); }
    @FXML private void handleOpenToday() {
        calendarController.today();
        navigationController.showCalendar();
    }
    @FXML private void handleOpenContacts() { navigationController.showContacts(); }
    @FXML private void handleToggleNavigation() {
        setSidebarExpanded(true, !leftSidebarWrapper.isManaged());
    }
    @FXML private void handleToggleAgenda() {
        setSidebarExpanded(false, !rightSidebarWrapper.isManaged());
    }

    private void initializeResizableSidebars() {
        setSidebarWidth(leftPanel, leftExpandedWidth);
        setSidebarWidth(rightPanel, rightExpandedWidth);
        installResizeBehavior(leftResizeHandle, leftPanel, true);
        installResizeBehavior(rightResizeHandle, rightPanel, false);
        appShell.widthProperty().addListener((observable, oldWidth, newWidth) ->
                handleShellWidthChanged(oldWidth.doubleValue(), newWidth.doubleValue()));
        updateSidebarToggleAccessibility();
    }

    private void installResizeBehavior(Region handle, VBox panel, boolean leftSidebar) {
        SidebarDragState drag = new SidebarDragState();
        handle.setCursor(Cursor.H_RESIZE);
        handle.setOnMousePressed(event -> {
            drag.startScreenX = event.getScreenX();
            drag.startWidth = panel.getWidth();
            drag.requestedWidth = drag.startWidth;
            drag.clip = new Rectangle();
            drag.clip.widthProperty().bind(panel.widthProperty());
            drag.clip.heightProperty().bind(panel.heightProperty());
            panel.setClip(drag.clip);
            event.consume();
        });
        handle.setOnMouseDragged(event -> {
            double horizontalMovement = event.getScreenX() - drag.startScreenX;
            drag.requestedWidth = drag.startWidth + (leftSidebar ? horizontalMovement : -horizontalMovement);
            double previewWidth = clamp(drag.requestedWidth, 0, maximumSidebarWidth(leftSidebar));
            setSidebarWidth(panel, previewWidth);
            event.consume();
        });
        handle.setOnMouseReleased(event -> {
            panel.setClip(null);
            if (drag.clip != null) {
                drag.clip.widthProperty().unbind();
                drag.clip.heightProperty().unbind();
                drag.clip = null;
            }
            if (drag.requestedWidth <= SIDEBAR_COLLAPSE_THRESHOLD) {
                setSidebarExpanded(leftSidebar, false);
            } else {
                double settledWidth = clamp(drag.requestedWidth,
                        minimumSidebarWidth(leftSidebar), maximumSidebarWidth(leftSidebar));
                rememberExpandedWidth(leftSidebar, settledWidth);
                setSidebarWidth(panel, settledWidth);
            }
            event.consume();
        });
        handle.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                setSidebarExpanded(leftSidebar, false);
                event.consume();
            }
        });
    }

    private void setSidebarExpanded(boolean leftSidebar, boolean expanded) {
        setSidebarExpanded(leftSidebar, expanded, false);
        if (expanded) fitSidebarsToWidth(appShell.getWidth(), leftSidebar);
    }

    private void setSidebarExpanded(boolean leftSidebar, boolean expanded, boolean automatic) {
        HBox wrapper = leftSidebar ? leftSidebarWrapper : rightSidebarWrapper;
        VBox panel = leftSidebar ? leftPanel : rightPanel;
        if (expanded) {
            double rememberedWidth = leftSidebar ? leftExpandedWidth : rightExpandedWidth;
            setSidebarWidth(panel, clamp(rememberedWidth,
                    minimumSidebarWidth(leftSidebar), maximumSidebarWidth(leftSidebar)));
        }
        wrapper.setManaged(expanded);
        wrapper.setVisible(expanded);
        setAutomaticallyCollapsed(leftSidebar, automatic && !expanded);
        updateSidebarToggleAccessibility();
    }

    private void handleShellWidthChanged(double oldWidth, double newWidth) {
        if (newWidth <= 0 || adjustingResponsiveSidebars) return;
        if (newWidth < oldWidth) {
            fitSidebarsToWidth(newWidth, null);
        } else if (newWidth > oldWidth) {
            restoreResponsiveSidebars(newWidth);
        }
    }

    /** Shrinks panels before hiding them, preserving the central workspace while the window narrows. */
    private void fitSidebarsToWidth(double shellWidth, Boolean preferredSidebar) {
        if (shellWidth <= 0 || adjustingResponsiveSidebars) return;
        adjustingResponsiveSidebars = true;
        try {
            boolean first = preferredSidebar == null ? false : !preferredSidebar;
            boolean second = !first;
            double deficit = workspaceDeficit(shellWidth);
            deficit = shrinkSidebar(first, deficit);
            deficit = shrinkSidebar(second, deficit);

            if (deficit > 0 && sidebarWrapper(first).isManaged()) {
                setSidebarExpanded(first, false, true);
                deficit = workspaceDeficit(shellWidth);
            }
            if (deficit > 0 && sidebarWrapper(second).isManaged()) {
                setSidebarExpanded(second, false, true);
            }
        } finally {
            adjustingResponsiveSidebars = false;
        }
    }

    /** Reopens only automatically hidden panels and progressively restores the user's chosen widths. */
    private void restoreResponsiveSidebars(double shellWidth) {
        if (adjustingResponsiveSidebars) return;
        adjustingResponsiveSidebars = true;
        try {
            restoreAutomaticallyCollapsedSidebar(true, shellWidth);
            restoreAutomaticallyCollapsedSidebar(false, shellWidth);
            if (!leftAutomaticallyCollapsed && !rightAutomaticallyCollapsed) {
                restoreRememberedWidths(shellWidth);
            }
        } finally {
            adjustingResponsiveSidebars = false;
        }
    }

    private void restoreAutomaticallyCollapsedSidebar(boolean leftSidebar, double shellWidth) {
        boolean automaticallyCollapsed = leftSidebar ? leftAutomaticallyCollapsed : rightAutomaticallyCollapsed;
        if (!automaticallyCollapsed) return;
        double requiredWidth = minimumSidebarWidth(leftSidebar) + RESIZE_HANDLE_WIDTH;
        double availableWidth = shellWidth - MIN_WORKSPACE_WIDTH - totalSidebarWidth();
        if (availableWidth >= requiredWidth) setSidebarExpanded(leftSidebar, true, true);
    }

    private void restoreRememberedWidths(double shellWidth) {
        double availableExtra = shellWidth - MIN_WORKSPACE_WIDTH - totalSidebarWidth();
        if (availableExtra <= 0) return;

        double leftNeed = widthToRestore(true);
        double rightNeed = widthToRestore(false);
        double totalNeed = leftNeed + rightNeed;
        if (totalNeed <= 0) return;

        double usableExtra = Math.min(availableExtra, totalNeed);
        double leftExtra = usableExtra * leftNeed / totalNeed;
        double rightExtra = usableExtra - leftExtra;
        if (leftNeed > 0) setSidebarWidth(leftPanel, leftPanel.getPrefWidth() + leftExtra);
        if (rightNeed > 0) setSidebarWidth(rightPanel, rightPanel.getPrefWidth() + rightExtra);
    }

    private double widthToRestore(boolean leftSidebar) {
        if (!sidebarWrapper(leftSidebar).isManaged()) return 0;
        double rememberedWidth = leftSidebar ? leftExpandedWidth : rightExpandedWidth;
        VBox panel = leftSidebar ? leftPanel : rightPanel;
        return Math.max(0, rememberedWidth - panel.getPrefWidth());
    }

    private double shrinkSidebar(boolean leftSidebar, double deficit) {
        if (deficit <= 0 || !sidebarWrapper(leftSidebar).isManaged()) return deficit;
        VBox panel = leftSidebar ? leftPanel : rightPanel;
        double currentWidth = panel.getPrefWidth();
        double reduction = Math.min(deficit, Math.max(0, currentWidth - minimumSidebarWidth(leftSidebar)));
        if (reduction > 0) setSidebarWidth(panel, currentWidth - reduction);
        return deficit - reduction;
    }

    private double workspaceDeficit(double shellWidth) {
        return Math.max(0, MIN_WORKSPACE_WIDTH + totalSidebarWidth() - shellWidth);
    }

    private double totalSidebarWidth() {
        return sidebarWidth(true) + sidebarWidth(false);
    }

    private double sidebarWidth(boolean leftSidebar) {
        if (!sidebarWrapper(leftSidebar).isManaged()) return 0;
        VBox panel = leftSidebar ? leftPanel : rightPanel;
        return panel.getPrefWidth() + RESIZE_HANDLE_WIDTH;
    }

    private HBox sidebarWrapper(boolean leftSidebar) {
        return leftSidebar ? leftSidebarWrapper : rightSidebarWrapper;
    }

    private void setAutomaticallyCollapsed(boolean leftSidebar, boolean automaticallyCollapsed) {
        if (leftSidebar) leftAutomaticallyCollapsed = automaticallyCollapsed;
        else rightAutomaticallyCollapsed = automaticallyCollapsed;
    }

    private void updateSidebarToggleAccessibility() {
        updateToggleAccessibility(leftSidebarToggleButton, leftSidebarWrapper.isManaged(), "navigation");
        updateToggleAccessibility(agendaToggleButton, rightSidebarWrapper.isManaged(), "agenda");
    }

    private void updateToggleAccessibility(Button button, boolean expanded, String panelName) {
        String description = (expanded ? "Hide " : "Show ") + panelName;
        button.setAccessibleText(description);
        if (button.getTooltip() != null) button.getTooltip().setText(description);
    }

    private double maximumSidebarWidth(boolean leftSidebar) {
        double configuredMaximum = leftSidebar ? LEFT_SIDEBAR_MAX_WIDTH : RIGHT_SIDEBAR_MAX_WIDTH;
        double shellWidth = appShell.getWidth();
        if (shellWidth <= 0) return configuredMaximum;
        double otherWidth = sidebarWidth(!leftSidebar);
        double available = shellWidth - otherWidth - MIN_WORKSPACE_WIDTH - RESIZE_HANDLE_WIDTH;
        return Math.max(minimumSidebarWidth(leftSidebar), Math.min(configuredMaximum, available));
    }

    private double minimumSidebarWidth(boolean leftSidebar) {
        return leftSidebar ? LEFT_SIDEBAR_MIN_WIDTH : RIGHT_SIDEBAR_MIN_WIDTH;
    }

    private void rememberExpandedWidth(boolean leftSidebar, double width) {
        if (leftSidebar) leftExpandedWidth = width;
        else rightExpandedWidth = width;
    }

    private void setSidebarWidth(VBox panel, double width) {
        panel.setMinWidth(width);
        panel.setPrefWidth(width);
        panel.setMaxWidth(width);
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static final class SidebarDragState {
        private double startScreenX;
        private double startWidth;
        private double requestedWidth;
        private Rectangle clip;
    }

    @FXML
    private void handleThemeToggle() {
        themeService.toggle();
        themeService.applyTo(themeToggleBtn.getScene());
        updateThemeButton();
        calendarController.refreshTheme();
        notesController.refreshTheme();
        if (currentUser == null) return;
        currentUser.setPreferredTheme(themeService.activeTheme().name());
        try {
            userRepository.save(currentUser);
        } catch (IllegalStateException failure) {
            dialogService.showError("Theme not saved",
                    "The theme was applied, but it could not be remembered for the next launch.");
        }
    }

    private void updateThemeButton() {
        themeToggleIcon.setIconLiteral(themeService.isBlueGrayTheme() || themeService.isGrayBlueTheme()
                ? "fas-palette"
                : themeService.isDarkMode() ? "fas-sun" : "fas-moon");
        themeToggleBtn.setText("Theme: " + themeService.activeTheme().displayName());
    }
}
