const punct = /!|"|:|;|—|\?|\.|,|—/i;
const letter = /[a-zA-Zа-яёА-ЯЁ\-]/i;
const letters = /[a-zA-Zа-яёА-ЯЁ\-]+/i;

const lineDelimetr = '\n';

// TODO: defis
export function isLetter(str) {
    if (!str) return false;
    return str.length === 1 && str
        .replace('\\', '') // needed to escape backslash
        .match(letter);
}

export function isHyphen(str) {
    return str === '-' || str === '—';
}

export function isWord(str) {
    if (!str) return false;
    return str.match(letters);
}

export function isPunctuationMark(str) {
    if (!str) return false;
    return str.length === 1 && str.match(punct);
}

export function getPunctuation(str) {
    if (!str) return false;
    let result;
    while (result = punct.exec(str)) {
        return str[result.index];
    }
}

// TODO: \r\n????
export function parseStanzas(str) {
    if (!str) return false;

    const stanzas = str.split(lineDelimetr + lineDelimetr).filter(Boolean);
    if (stanzas.length > 1) {
        for (let i = 0; i < stanzas.length; i++) {
            const lines = parseStanza(stanzas[i]);
            for (let j = 0; j < lines.length; j++) {
                lines[j] = parseLine(lines[j]);
            }
            stanzas[i] = lines;
        }
        return stanzas;
    } else {
        const lines = parseStanza(str);
        if (lines.length > 1) {
            for (let j = 0; j < lines.length; j++) {
                lines[j] = parseLine(lines[j]);
            }
            if (str[str.length - 1] === lineDelimetr + lineDelimetr) {
                return lines;
            }
            return { lines };
        } else {
            const words = parseLine(str);
            if (words.length > 1) {
                // separate line
                if (str[str.length - 1] === lineDelimetr) {
                    return words;
                }
                return { words }
            }
            return str;
        }
    }
}

export function parseStanza(str) {
    if (!str) return false;
    return str.split(lineDelimetr).filter(Boolean);
}

export function parseLine(str) {
    if (!str) return false;
    return str.split(/\s+|([,\—.;:?!()])/).filter(Boolean);
}

export function capitalize(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

export function stanzasToString(stanzas) {
    return stanzas.reduce((res, stanza, s) => {
        return res + stanza.reduce((lineRes, line) => {
            return lineRes + line.join(' ') + lineDelimetr;
        }, '') + (s === stanzas.length - 1 ? '' : lineDelimetr);
    }, '');
}