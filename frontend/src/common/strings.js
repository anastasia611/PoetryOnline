const punct = /!|"|:|;|—|\?|\.|,|—/i;
const letter = /[a-zA-Zа-яёА-ЯЁ\-]/i;
const letters = /[a-zA-Zа-яёА-ЯЁ\-]+/i;

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

export function parseWords(str) {
    if (!str) return false;
    return str.split(/\s+|([,\—.;:?!()])/).filter(Boolean);
}

export function capitalize(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

export function stanzasToString(stanzas) {
    return stanzas.reduce((res, stanza, s) => {
        return res + stanza.reduce((lineRes, line) => {
            return lineRes + line.join(' ') + '\n';
        }, '') + (s === stanzas.length - 1 ? '' : '\n');
    }, '');
}