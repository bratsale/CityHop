package project.pj25.model;

/**
 * Enumeracija koja definiše tipove saobraćajnih stanica.
 * <p>
 * Koristi se za jasnu i tipski sigurnu reprezentaciju tipa prevoza,
 * što olakšava rad sa podacima i sprečava greške.
 * </p>
 *
 * @author bratsale
 * @version 1.0
 */
public enum StationType {
    /**
     * Predstavlja autobusku stanicu ili autobuski prevoz.
     */
    AUTOBUS,

    /**
     * Predstavlja željezničku stanicu ili željeznički prevoz.
     */
    VOZ
}