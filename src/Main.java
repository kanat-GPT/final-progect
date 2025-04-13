import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.Serial;

class Ingredient implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String name;
    private final double quantity;
    private final String unit;

    public Ingredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    @Override
    public String toString() {
        return quantity + " " + unit + " " + name;
    }
}

class Recipe implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;
    private String category;
    private final List<Ingredient> ingredients;
    private String instructions;

    public Recipe(String name, String category) {
        this.name = name;
        this.category = category;
        this.instructions = "";
        this.ingredients = new ArrayList<>();
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    @Override
    public String toString() {
        return "Рецепт: " + name + "\nКатегория: " + category +
                "\nИнгредиенты:\n" + ingredients +
                "\nИнструкции: " + instructions;
    }
}

class RecipeManager {
    private List<Recipe> recipes;
    private static final String FILE_NAME = "recipes.dat";

    public RecipeManager() {
        recipes = new ArrayList<>();
        loadRecipes();
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
        saveRecipes();
    }

    public void deleteRecipe(String recipeName) {
        recipes.removeIf(recipe -> recipe.getName().equalsIgnoreCase(recipeName));
        saveRecipes();
    }

    public Recipe findRecipe(String recipeName) {
        for (Recipe recipe : recipes) {
            if (recipe.getName().equalsIgnoreCase(recipeName)) {
                return recipe;
            }
        }
        return null;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void saveRecipes() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(recipes);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении рецептов: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadRecipes() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                recipes = (List<Recipe>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Ошибка при загрузке рецептов: " + e.getMessage());
                recipes = new ArrayList<>();
            }
        }
    }
}

public class Main {
    private static final RecipeManager manager = new RecipeManager();
    private static JFrame frame;
    private static JTextArea recipeDisplay;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Управление рецептами");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Добавить рецепт");
        JButton editButton = new JButton("Редактировать рецепт");
        JButton deleteButton = new JButton("Удалить рецепт");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        recipeDisplay = new JTextArea(15, 50);
        recipeDisplay.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(recipeDisplay);

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Removed unused 'e' parameters
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> showDeleteDialog());

        updateRecipeDisplay();
        frame.setVisible(true);
    }

    private static void showAddDialog() {
        JDialog dialog = new JDialog(frame, "Добавить рецепт", true);
        dialog.setLayout(new GridLayout(5, 2));
        dialog.setSize(300, 200);

        JTextField nameField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField ingredientField = new JTextField();
        JTextArea instructionsArea = new JTextArea(3, 20);

        dialog.add(new JLabel("Название:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Категория:"));
        dialog.add(categoryField);
        dialog.add(new JLabel("Ингредиент (название, кол-во, единица):"));
        dialog.add(ingredientField);
        dialog.add(new JLabel("Инструкции:"));
        dialog.add(new JScrollPane(instructionsArea));

        JButton saveButton = new JButton("Сохранить");
        // Removed unused 'e' parameter
        saveButton.addActionListener(e -> {
            Recipe recipe = new Recipe(nameField.getText(), categoryField.getText());
            String[] ingParts = ingredientField.getText().split(",");
            if (ingParts.length == 3) {
                try {
                    recipe.addIngredient(new Ingredient(
                            ingParts[0].trim(),
                            Double.parseDouble(ingParts[1].trim()),
                            ingParts[2].trim()
                    ));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Неверный формат количества!");
                    return;
                }
            }
            recipe.setInstructions(instructionsArea.getText());
            manager.addRecipe(recipe);
            updateRecipeDisplay();
            dialog.dispose();
        });

        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private static void showEditDialog() {
        String name = JOptionPane.showInputDialog("Введите название рецепта для редактирования:");
        Recipe recipe = manager.findRecipe(name);
        if (recipe != null) {
            JDialog dialog = new JDialog(frame, "Редактировать рецепт", true);
            dialog.setLayout(new GridLayout(3, 2));
            dialog.setSize(300, 150);

            JTextField nameField = new JTextField(recipe.getName());
            JTextField categoryField = new JTextField(recipe.getCategory());

            dialog.add(new JLabel("Название:"));
            dialog.add(nameField);
            dialog.add(new JLabel("Категория:"));
            dialog.add(categoryField);

            JButton saveButton = new JButton("Сохранить");
            // Removed unused 'e' parameter
            saveButton.addActionListener(e -> {
                recipe.setName(nameField.getText());
                recipe.setCategory(categoryField.getText());
                manager.saveRecipes();
                updateRecipeDisplay();
                dialog.dispose();
            });

            dialog.add(saveButton);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame, "Рецепт не найден!");
        }
    }

    private static void showDeleteDialog() {
        String name = JOptionPane.showInputDialog("Введите название рецепта для удаления:");
        if (name != null && !name.trim().isEmpty()) {
            manager.deleteRecipe(name);
            updateRecipeDisplay();
        }
    }

    private static void updateRecipeDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Recipe recipe : manager.getRecipes()) {
            sb.append(recipe.toString()).append("\n-------------------\n");
        }
        recipeDisplay.setText(sb.toString());
    }
}