package com.pierrejacquier.olim.data;

import com.pierrejacquier.olim.helpers.Tools;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class User {
    private String fullName;
    private String email;
    private List<Task> tasks;
    private List<Tag> tags;

    public User() {
        User(null, null, new ArrayList<Task>(), new ArrayList<Tag>());
    }

    public User(String fullName, String email) {
        User(fullName, email, new ArrayList<Task>(), new ArrayList<Tag>());
    }

    public User(String fullName, String email, List<Task> tasks, List<Tag> tags) {
        User(fullName, email, tasks, tags);
    }

    private void User(String fullName, String email, List<Task> tasks, List<Tag> tags) {
        this.fullName = fullName;
        this.email = email;
        this.tasks = tasks;
        this.tags = tags;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public List<Tag> getTags() {
        /*
        List <Tag> tags = new ArrayList<>();

        Document[] tagsDocs = MeteorSingleton.getInstance()
                .getDatabase()
                .getCollection("Tags")
                .find();
        for (Document tag : tagsDocs) {
            tags.add(new Tag(tag));
        }
*/
        return this.tags;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Task> getTasks() {
        return this.getTasks(null, false);
    }

    public List<Task> getTasks(boolean excludeDone) {
        return this.getTasks(null, excludeDone);
    }

    public List<Task> getTasks(Tag tag) {
        return this.getTasks(tag, false);
    }

    public List<Task> getTasks(Tag tag, boolean excludeDone) {
        List <Task> tasks = new ArrayList<>();

        /*Query tasksQuery = MeteorSingleton.getInstance()
                .getDatabase()
                .getCollection("Tasks");

        if (tag != null) {
            tasksQuery.whereEqual("tag", tag.getId());
        }

        if (excludeDone) {
            tasksQuery.whereEqual("done", false);
        }

        Document[] tasksDocs = tasksQuery.find();

        for (Document task : tasksDocs) {
            tasks.add(new Task());
        }

        Collections.sort(tasks, new Comparator<Task>() {

            @Override
            public int compare(Task task2, Task task1) {
                return task2.getDueDate().compareTo(task1.getDueDate());
            }
        });*/

        return this.tasks;
    }

    public List<Task> getOverdueTasks() {
        return getOverdueTasks(null);
    }

    public List<Task> getTodayTasks() {
        return getTodayTasks(null);
    }

    public List<Task> getTomorrowTasks() {
        return getTomorrowTasks(null);
    }

    public List<Task> getInTheNextSevenDaysTasks() {
        return getInTheNextSevenDaysTasks(null);
    }

    public List<Task> getLaterTasks() {
        return getLaterTasks(null);
    }

    public List<Task> getOverdueTasks(Tag tag) {
        List<Task> tasks = this.getTasks(tag, true);
        List<Task> overdueTasks = new ArrayList<>();
        Calendar dueDate = Calendar.getInstance();

        Calendar today = Calendar.getInstance();
        Tools.setStartOfDay(today);

        for (Task task : tasks) {
            dueDate.setTime(task.getDueDate());

            if (today.after(dueDate)) {
                overdueTasks.add(task);
            }
        }

        return overdueTasks;
    }

    public List<Task> getTodayTasks(Tag tag) {
        List<Task> tasks = this.getTasks(tag);
        List<Task> todayTasks = new ArrayList<>();
        Calendar dueDate = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        Tools.setStartOfDay(today);

        for (Task task : tasks) {
            dueDate.setTime(task.getDueDate());

            if (today.get(Calendar.DAY_OF_MONTH) == dueDate.get(Calendar.DAY_OF_MONTH)) {
                todayTasks.add(task);
            }
        }

        return todayTasks;
    }

    public List<Task> getTomorrowTasks(Tag tag) {
        List<Task> tasks = this.getTasks(tag);
        List<Task> tomorrowTasks = new ArrayList<>();
        Calendar dueDate = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();

        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        for (Task task : tasks) {
            dueDate.setTime(task.getDueDate());

            if (tomorrow.get(Calendar.DAY_OF_MONTH) == dueDate.get(Calendar.DAY_OF_MONTH)) {
                tomorrowTasks.add(task);
            }
        }

        return tomorrowTasks;
    }

    public List<Task> getInTheNextSevenDaysTasks(Tag tag) {
        List<Task> tasks = this.getTasks(tag);
        List<Task> inTheNextSevenDaysTasks = new ArrayList<>();
        Calendar dueDate = Calendar.getInstance();
        Calendar inTheNextSevenDaysStart = Calendar.getInstance();
        Calendar inTheNextSevenDaysEnd = Calendar.getInstance();
        inTheNextSevenDaysStart.add(Calendar.DAY_OF_MONTH, 2);
        inTheNextSevenDaysEnd.add(Calendar.DAY_OF_MONTH, 7);
        Tools.setStartOfDay(inTheNextSevenDaysStart);
        Tools.setStartOfDay(inTheNextSevenDaysEnd);

        for (Task task : tasks) {
            dueDate.setTime(task.getDueDate());

            if (inTheNextSevenDaysStart.before(dueDate) && inTheNextSevenDaysEnd.after(dueDate)) {
                inTheNextSevenDaysTasks.add(task);
            }
        }

        return inTheNextSevenDaysTasks;
    }

    public List<Task> getLaterTasks(Tag tag) {
        List<Task> tasks = this.getTasks(tag);
        List<Task> laterTasks = new ArrayList<>();
        Calendar dueDate = Calendar.getInstance();
        Calendar inTheNextSevenDaysEnd = Calendar.getInstance();
        inTheNextSevenDaysEnd.add(Calendar.DAY_OF_MONTH, 8);
        Tools.setStartOfDay(inTheNextSevenDaysEnd);

        for (Task task : tasks) {
            dueDate.setTime(task.getDueDate());

            if (inTheNextSevenDaysEnd.before(dueDate)) {
                laterTasks.add(task);
            }
        }

        return laterTasks;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        result.append( this.getClass().getName() );
        result.append( " Object {" );
        result.append(newLine);
        Field[] fields = this.getClass().getDeclaredFields();
        for ( Field field : fields  ) {
            result.append("  ");
            try {
                result.append( field.getName() );
                result.append(": ");
                result.append( field.get(this) );
            } catch ( IllegalAccessException ex ) {  }
            result.append(newLine);
        }
        result.append("}");
        return result.toString();
    }
}
