const punct = /!|"|:|;|—|\?|\.|,/i;
const letter = /[a-zA-zа-яёА-ЯЁ\-]/i;
const letters = /[a-zA-zа-яёА-ЯЁ\-]+/i;

// TODO: defis
export function isLetter(str) {
    if (!str) return false;
    return str.length === 1 && str
        .replace('\\', '') // needed to escape backslash
        .match(letter);
}

export function isWord(str) {
    if (!str) return false;
    return str.match(letters);
}

export function isPunctuationMark(str) {
    if (!str) return false;
    return str.length === 1 && str.match(punct);
}

export function parseWords(str) {
    if (!str) return false;
    return str.split(/\s+|([,\—.;:?!()])/).filter(Boolean);
}

export function capitalize(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}