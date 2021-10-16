import { push, remove } from "../common/arrays";
import { writable } from 'svelte/store';

/**
 * Poem datastore
 */
export class PoemDataStore {
    /**
     * @param stanzas Array<Stanza>
     */
    constructor(stanzas) {
        this.stanzasStore = writable(stanzas);
        this.stanzaIndexStore = writable(0);
        this.lineIndexStore = writable(0);
        this.wordIndexStore = writable(0);

        this.stanzas = stanzas;
        this.stanzaIndex = -1;
        this.lineIndex = -1;
        this.wordIndex = -1;
    }

    subscribeStanzaIndex(callback) {
        this.stanzaIndexStore.subscribe(value => {
            this.stanzaIndex = value;
            callback(value);
        });
    }

    subscribeLineIndex(callback) {
        this.lineIndexStore.subscribe(value => {
            this.lineIndex = value;
            callback(value);
        });
    }

    subscribeWordIndex(callback) {
        this.wordIndexStore.subscribe(value => {
            this.wordIndex = value;
            callback(value);
        });
    }

    subscribe(callback) {
        this.stanzasStore.subscribe(value => {
            this.stanzas = value;
            callback(value);
        });
    }

    getSize() {
        return this.stanzas.length;
    };

    getStanzas() {
        return this.stanzas;
    };

    getStanza(s = this.stanzaIndex) {
        return this.stanzas[s];
    };

    getStanzaSize(s = this.stanzaIndex) {
        if (this.getStanza(s)) {
            return this.getStanza(s).length;
        }
    };

    setStanza(lines, s = this.stanzaIndex) {
        this.stanzas[s] = lines;
        this.stanzasStore.set(this.stanzas);
    };

    addStanza(lines = [ [ '' ] ], s = this.stanzaIndex) {
        this.setStanzaIndex(++s);
        this.setIndexes(s, 0, 0);
        this.stanzasStore.set(push(this.stanzas, s, lines));
    };

    removeStanza(s = this.stanzaIndex) {
        this.setStanzaIndex(s);

        // if the last stanza, set the last line of previous one
        if (s === this.stanzas.length - 1 && s > 0) {
            this.setStanzaIndex(s - 1);
            const stanza = this.getStanza(s);
            if (stanza.length) {
                this.setLineIndex(stanza.length - 1);
            }
        } else {  // set the first line of the next one
            this.setLineIndex(0);
        }

        // leave at least one stanza
        if (this.stanzas.length > 1) {
            remove(this.stanzas, s);
        } else {
            this.setStanza([ [ '' ] ], 0);
            this.setIndexes(0, 0, 0);
        }
    };

    getLine(l = this.lineIndex, s = this.stanzaIndex) {
        const stanza = this.getStanza(s);
        if (stanza) {
            return stanza[l];
        }
    };

    getLineSize(l = this.lineIndex, s = this.stanzaIndex) {
        if (this.getLine(l, s)) {
            return this.getLine(l, s).length;
        }
    };

    setLine(words, l = this.lineIndex, s = this.stanzaIndex) {
        const stanza = this.getStanza(s);
        if (stanza) {
            this.stanzas[s][l] = words;
            this.stanzasStore.set(this.stanzas);
        }
    };

    addLine(words = [ '' ], l = this.lineIndex, s = this.stanzaIndex) {
        this.setLineIndex(++l);
        this.setStanza(push(this.getStanza(s), l, words));
        this.stanzasStore.set(this.stanzas);
    };

    removeLine(l = this.lineIndex, s = this.stanzaIndex) {
        const stanza = this.getStanza(s);
        if (stanza) {
            console.log('rm ln', s, l, this.getStanza(s));
            remove(this.getStanza(s), l);
            this.stanzasStore.set(this.stanzas)
            this.setStanza((this.getStanza())); // hack
            if (l > 0) {
                this.setLineIndex(--l);
            }
            this.setWordIndex(0);
            // console.log('IDX', this.stanzaIndex, this.lineIndex, this.wordIndex)
        }
    };

    isLineEmpty(line) {
        return !line || !line.length || line.length === 1 && !line[0];
    };

    getWord(w = this.wordIndex, l = this.lineIndex, s = this.stanzaIndex) {
        const line = this.getLine(l, s);
        if (line) {
            return line[w];
        }
    };

    getLastWord(l = this.lineIndex, s = this.stanzaIndex) {
        const line = this.getLine(l, s);
        if (line) {
            return line[line.length - 1];
        }
    };

    setLastWord(word, l = this.lineIndex, s = this.stanzaIndex) {
        const line = this.getLine(l, s);
        if (line) {
            line[line.length - 1] = word;
        }
    };

    setWord(word, w = this.wordIndex, l = this.lineIndex, s = this.stanzaIndex) {
        const line = this.getLine(l, s);
        if (line) {
            line[w] = word;
        }
    };

    addWord(word, w = this.wordIndex, l = this.lineIndex, s = this.stanzaIndex) {
        this.setWordIndex(++w);
        this.setLine(push(this.getLine(l, s), w, word), l, s);
        this.stanzasStore.set(this.stanzas);
    };

    removeWord(w = this.wordIndex, l = this.lineIndex, s = this.stanzaIndex) {
        const line = this.getLine(l, s);
        if (line) {
            remove(line, w);
            this.stanzasStore.set(this.stanzas);
        }
    };

    setIndexes(s, l, w) {
        // console.log('idx', s, l, w);
        this.setWordIndex(w);
        this.setLineIndex(l);
        this.setStanzaIndex(s);
    };

    setWordIndex(w) {
        if (typeof w !== 'undefined') {
            this.wordIndexStore.set(w);
        }
    };

    setLineIndex(l) {
        if (typeof l !== 'undefined') {
            this.lineIndexStore.set(l);
        }
    };

    setStanzaIndex(s) {
        if (typeof s !== 'undefined') {
            this.stanzaIndexStore.set(s);
        }
    };

    isLastWordInLine(w, l, s) {
        if (this.getLine(l, s)) {
            return w === this.getLine(l, s).length - 1;
        }
    }

    isLastLineInStanza(l, s) {
        if (this.getStanza(s)) {
            return l === this.getStanza(s).length - 1;
        }
    }

    isBeginPoem() {
        return this.wordIndex === 0 && this.lineIndex === 0 && this.stanzaIndex === 0;
    };

    isEndPoem() {
        const stanza = this.stanzas[this.stanzaIndex];
        const line = stanza[this.lineIndex];

        return this.wordIndex === line.length - 1 && this.lineIndex === stanza.length - 1 && this.stanzaIndex === this.stanzas.length - 1;
    };
}