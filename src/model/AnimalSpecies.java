package model;

public enum AnimalSpecies {
    JAGUAR, SLOTH, SCARLET_MACAW, WHITE_FACED_MONKEY, TOUCAN, RED_EYED_TREE_FROG, COATI;

    /** Texto del JSON → especie. */
    public static AnimalSpecies fromName(String name) {
        if (name == null) {
            return null;
        }
        String clean = name.trim();
        for (AnimalSpecies species : values()) {
            if (species.name().equalsIgnoreCase(clean)) {
                return species;
            }
        }
        return null; // no existe esa especie: el handler responde MALFORMED_MESSAGE
    }
}
