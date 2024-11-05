import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

// Class representing an individual article
class Article {
    String title;
    String content;
    String author;
    Article next;
    Article prev;

    public Article(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.next = null;
        this.prev = null;
    }
}

// Circular Doubly Linked List for storing articles with FIFO behavior
class CircularDoublyLinkedList {
    private Article head;
    private int maxSize;
    private int currentSize;

    public CircularDoublyLinkedList(int maxSize) {
        this.head = null;
        this.maxSize = maxSize;
        this.currentSize = 0;
    }

    // Add a new article to the list
    public void addArticle(String title, String content, String author) {
        Article newArticle = new Article(title, content, author);
        if (head == null) {
            head = newArticle;
            newArticle.next = head;
            newArticle.prev = head;
        } else {
            Article tail = head.prev;
            tail.next = newArticle;
            newArticle.prev = tail;
            newArticle.next = head;
            head.prev = newArticle;
        }
        currentSize++;

        // If the list exceeds the max size, remove the oldest article
        if (currentSize > maxSize) {
            deleteOldestArticle();
        }
    }

    // Remove the oldest article (FIFO behavior)
    private void deleteOldestArticle() {
        if (head != null && currentSize > 0) {
            if (head.next == head) {
                head = null; // Only one article
            } else {
                Article tail = head.prev;
                head = head.next;
                head.prev = tail;
                tail.next = head;
            }
            currentSize--;
        }
    }

    // Sort articles alphabetically by title using bubble sort
    public void sortArticles() {
        if (head == null || head.next == head) return;
        boolean swapped;
        do {
            swapped = false;
            Article current = head;
            do {
                Article nextArticle = current.next;
                if (current.title.compareTo(nextArticle.title) > 0) {
                    // Swap articles
                    String tempTitle = current.title;
                    String tempContent = current.content;
                    String tempAuthor = current.author;

                    current.title = nextArticle.title;
                    current.content = nextArticle.content;
                    current.author = nextArticle.author;

                    nextArticle.title = tempTitle;
                    nextArticle.content = tempContent;
                    nextArticle.author = tempAuthor;

                    swapped = true;
                }
                current = current.next;
            } while (current.next != head);
        } while (swapped);
    }

    // Display all articles in the list
    public String displayArticles() {
        if (head == null) {
            return "No articles available.";
        }
        StringBuilder result = new StringBuilder();
        result.append(String.format("%-20s%-20s%-30s\n", "Title", "Author", "Content"));
        result.append("-------------------------------------------------------------\n");

        Article current = head;
        do {
            result.append(String.format("%-20s%-20s%-30s\n", current.title, current.author, current.content));
            current = current.next;
        } while (current != head);

        return result.toString();
    }

    // Search for an article by title
    public String searchArticle(String title) {
        if (head == null) {
            return "No articles available.";
        }

        Article current = head;
        do {
            if (current.title.equalsIgnoreCase(title)) {
                return String.format("Title: %-20s\nAuthor: %-20s\nContent: %-30s\n", current.title, current.author, current.content);
            }
            current = current.next;
        } while (current != head);

        return "Article with title '" + title + "' not found.";
    }

    // Get the current article in round-robin order
    public Article getCurrentArticle(Article current) {
        if (head == null) {
            return null;
        }
        return current == null ? head : current.next;
    }
}

// Welcome page for the application
class WelcomePage extends Frame implements ActionListener {
    Button continueButton;

    public WelcomePage() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Welcome Message
        Label welcomeLabel = new Label("Welcome to the Article Management System!", Label.CENTER);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(welcomeLabel, gbc);

        // About Us section
        Label aboutLabel = new Label("About Us: We provide reliable and timely news articles.");
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(aboutLabel, gbc);

        // Contact Information
        Label contactLabel = new Label("Contact Us: Email - support@articles.com, Phone - 123-456-7890");
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(contactLabel, gbc);

        // Continue Button
        continueButton = new Button("Continue to Article App");
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(continueButton, gbc);
        continueButton.addActionListener(this);

        // Window settings
        setTitle("Welcome");
        setSize(500, 300);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    // Action handler for Continue button
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == continueButton) {
            this.dispose(); // Close WelcomePage
            new ArticleApp(); // Open ArticleApp
        }
    }
}

// Article management application using AWT
class ArticleApp extends Frame implements ActionListener {
    TextField titleField, contentField, authorField, searchField;
    TextArea displayArea;
    Button addButton, sortButton, viewButton, stopButton, searchButton, addToFavButton, displayFavButton;
    CircularDoublyLinkedList articleList = new CircularDoublyLinkedList(10); // Max size is 10
    Article currentArticle = null;
    boolean running = false;
    Thread roundRobinThread;

    // Stack for storing favorite articles
    Stack<Article> favorites = new Stack<>();

    public ArticleApp() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Set background color
        setBackground(Color.lightGray);

        // Title Label and TextField
        gbc.gridx = 0; gbc.gridy = 0;
        add(new Label("Title: "), gbc);
        gbc.gridx = 1;
        titleField = new TextField(20);
        add(titleField, gbc);

        // Author Label and TextField
        gbc.gridx = 0; gbc.gridy = 1;
        add(new Label("Author: "), gbc);
        gbc.gridx = 1;
        authorField = new TextField(20);
        add(authorField, gbc);

        // Content Label and TextField
        gbc.gridx = 0; gbc.gridy = 2;
        add(new Label("Content: "), gbc);
        gbc.gridx = 1;
        contentField = new TextField(20);
        add(contentField, gbc);

        // Search Label and TextField
        gbc.gridx = 0; gbc.gridy = 3;
        add(new Label("Search Title: "), gbc);
        gbc.gridx = 1;
        searchField = new TextField(20);
        add(searchField, gbc);

        // Add Button
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        addButton = new Button("Add Article");
        add(addButton, gbc);
        addButton.addActionListener(this);

        // Sort Button
        gbc.gridy = 5;
        sortButton = new Button("Sort Articles");
        add(sortButton, gbc);
        sortButton.addActionListener(this);

        // Start Round Robin Button
        gbc.gridy = 6;
        viewButton = new Button("Start Round Robin");
        add(viewButton, gbc);
        viewButton.addActionListener(this);

        // Stop Round Robin Button
        gbc.gridy = 7;
        stopButton = new Button("Stop Round Robin");
        add(stopButton, gbc);
        stopButton.addActionListener(this);

        // Search Button
        gbc.gridy = 8;
        searchButton = new Button("Search Article");
        add(searchButton, gbc);
        searchButton.addActionListener(this);

        // Add to Favorites Button
        gbc.gridy = 9;
        addToFavButton = new Button("Add to Favorites");
        add(addToFavButton, gbc);
        addToFavButton.addActionListener(this);

        // Display Favorites Button
        gbc.gridy = 10;
        displayFavButton = new Button("Display Favorites");
        add(displayFavButton, gbc);
        displayFavButton.addActionListener(this);

        // TextArea for displaying articles
        gbc.gridy = 11;
        displayArea = new TextArea(10, 50);
        gbc.gridwidth = 2;
        add(displayArea, gbc);

        // Window settings
        setTitle("Article Management App");
        setSize(600, 600);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    // Action handler for buttons
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == addButton) {
            String title = titleField.getText();
            String content = contentField.getText();
            String author = authorField.getText();
            if (!title.isEmpty() && !content.isEmpty() && !author.isEmpty()) {
                articleList.addArticle(title, content, author);
                displayArea.setText(articleList.displayArticles());
                clearInputFields();
            }
        } else if (ae.getSource() == sortButton) {
            articleList.sortArticles();
            displayArea.setText(articleList.displayArticles());
        } else if (ae.getSource() == viewButton) {
            startRoundRobin();
        } else if (ae.getSource() == stopButton) {
            stopRoundRobin();
        } else if (ae.getSource() == searchButton) {
            String title = searchField.getText();
            displayArea.setText(articleList.searchArticle(title));
        } else if (ae.getSource() == addToFavButton) {
            if (currentArticle != null) {
                favorites.push(currentArticle);
                displayArea.setText("Article added to favorites: " + currentArticle.title);
            } else {
                displayArea.setText("No article available to add to favorites.");
            }
        } else if (ae.getSource() == displayFavButton) {
            if (favorites.isEmpty()) {
                displayArea.setText("No favorite articles available.");
            } else {
                StringBuilder favoriteList = new StringBuilder();
                favoriteList.append("Favorite Articles:\n");
                for (Article article : favorites) {
                    favoriteList.append(article.title).append("\n");
                }
                displayArea.setText(favoriteList.toString());
            }
        }
    }

    // Clear input fields
    private void clearInputFields() {
        titleField.setText("");
        contentField.setText("");
        authorField.setText("");
    }

    // Start round-robin display of articles
    private void startRoundRobin() {
        if (!running) {
            running = true;
            roundRobinThread = new Thread(() -> {
                while (running) {
                    try {
                        Thread.sleep(2000);
                        currentArticle = articleList.getCurrentArticle(currentArticle);
                        displayArea.setText(String.format("Title: %-20s\nAuthor: %-20s\nContent: %-30s\n", currentArticle.title, currentArticle.author, currentArticle.content));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            roundRobinThread.start();
        }
    }

    // Stop round-robin display of articles
    private void stopRoundRobin() {
        running = false;
        if (roundRobinThread != null) {
            roundRobinThread.interrupt();
        }
        displayArea.setText("Round-robin display stopped.");
    }
}

// Main class to launch the application
public class ArticleManagementSystem {
    public static void main(String[] args) {
        new WelcomePage();
    }
}