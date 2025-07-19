package project.pj25.model;

import java.util.Objects;

public class City {
    private int id; // Jedinstveni ID za grad, npr. x * m + y
    private int x;  // Red u matrici
    private int y;  // Kolona u matrici
    private String name; // Možemo dodati ime, ili je ID dovoljan

    public City(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.name = "G_" + x + "_" + y; // U skladu sa generatorom
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    // Opciono: Setteri ako je potrebno mijenjati atribute nakon kreiranja
    // public void setId(int id) { this.id = id; }
    // public void setX(int x) { this.x = x; }
    // public void setY(int y) { this.y = y; }
    // public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    // Override equals i hashCode za pravilno poređenje objekata
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return id == city.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}