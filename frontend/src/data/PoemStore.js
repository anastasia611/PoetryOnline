import { push, remove } from "../common/arrays";
import { stanzaIndex, stanzas } from "../Poem.svelte";

/**
 * Poem datastore
 */
export class PoemDatastore {
    /**
     * @param stanzas Array<Stanza>
     */
    constructor(stanzas) {
        this.stanzas = stanzas;
        this.stanzaIndex = -1;
        this.lineIndex = -1;
        this.wordIndex = -1;
    }

    getStanza = (s = this.stanzaIndex) => {
        return this.stanzas[s];
    };

    setStanza = (lines, s = this.stanzaIndex) => {
        this.stanzas[s] = lines;
    };

    addStanza = (lines = [['']], s = this.stanzaIndex) => {
        ++this.stanzaIndex;
        this.lineIndex = 0;
        this.wordIndex = 0;
        this.stanzas = push(this.stanzas, s, lines);
    };

    removeStanza = (s = this.stanzaIndex) => {
        remove(this.stanzas, s);
        ++this.stanzaIndex;
        setLineIndex(getStanza().length - 1);
    };
}