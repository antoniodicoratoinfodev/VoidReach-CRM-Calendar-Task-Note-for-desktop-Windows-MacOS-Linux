# CRM Management System

A desktop CRM (Customer Relationship Management) application built with Java and JavaFX, featuring a modern and clean interface designed for professional contact and activity management.

---

## Features

### Contact Management
- **Contact Table** — displays contacts with the following fields: Name, Company, Job Title, Email, Phone, Last Interaction, and Tag.
- **Add Contact** — create new contacts via a dedicated dialog with all fields and tag assignment.
- **Edit Contact** — double-click any row to open the edit dialog and modify any field.
- **Delete Contact** — select a contact and press `Delete` or `Backspace`; a confirmation dialog prevents accidental deletions.
- **Color-coded Tags** — contacts can be labeled as **Client**, **Tech**, or **Follow-up**, each rendered with a distinct color badge.

### Search & Filtering
- **Real-time search** across Name, Company, and Email fields — results update instantly as you type.

### Pagination
- Configurable rows per page: **15**, **25**, **50**, **100**, or **All**.
- Live pagination info label showing the current range and total contacts (e.g. *Showing 1–15 of 103 Contacts*).

### Calendar View
- **Day-view timeline** with a full 24-hour grid.
- **Create tasks** by clicking on any time slot on the timeline.
- **Drag & drop tasks** to reschedule them by moving them along the timeline.
- **Resize tasks** by dragging the bottom handle of any task block.
- **Edit tasks** by double-clicking a task or right-clicking it — opens a dialog to modify title, start/end time, color, and description.
- **Delete tasks** by selecting a task and pressing `Delete` or `Backspace`.
- **Task colors** — Blue, Red, Green, Yellow, Orange, Purple.
- **Date navigation** — previous day, next day, and jump-to-today buttons.

### Right Sidebar
- **Mini calendar** — monthly view with navigation arrows for month-by-month browsing; the selected day is highlighted in blue, today is outlined.
- **Upcoming Activities** — list of tasks for the selected day, showing time slot and title. Clicking an activity navigates to the Calendar view.

### Theme
- **Dark / Light mode toggle** — switch between themes at any time; the button icon updates accordingly (sun for dark mode, moon for light mode).

### Navigation
The left sidebar provides access to all sections:
- **Dashboard** *(placeholder)*
- **Contacts** — fully implemented
- **Calendar** — fully implemented
- **Leads** *(placeholder)*
- **Opportunities** *(placeholder)*
- **Accounts** *(placeholder)*
- **Tasks** *(placeholder)*
- **Settings** *(placeholder)*

---

## Requirements

- Java JDK 17 or higher (developed and tested on JDK 25)
- Apache Maven

---

## How to Run

Clone the repository and, from the `CRM-APP` directory, run:

```bash
mvn clean javafx:run
```

Or use the provided script:

```bash
./run.sh
```

The project can also be opened and run directly from IntelliJ IDEA.

---

## Project Structure

```
CRM-APP/
├── src/
│   └── main/
│       ├── java/com/crm/
│       │   ├── app/
│       │   │   ├── Main.java          # JavaFX entry point
│       │   │   └── AppLauncher.java   # Launcher wrapper
│       │   ├── controller/
│       │   │   └── MainController.java # All UI logic
│       │   └── model/
│       │       └── Contact.java       # Contact data model
│       └── resources/
│           ├── com/crm/view/
│           │   └── MainView.fxml      # UI layout
│           ├── css/
│           │   ├── style-dark.css     # Dark theme
│           │   └── style.css          # Light theme
│           └── images/
│               └── app-icon.png       # Application icon
└── pom.xml
```

---

## License

See [LICENSE](LICENSE) for terms of use.
