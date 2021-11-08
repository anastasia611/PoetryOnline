import { push } from "../common/arrays";

const currentRevisionIdxKey = "current-revision";
const revisionsKey = "poem-revisions";

const MAX_HISTORY_SIZE = 50;

// clearHistory();

export function getLatest() {
    return getRevisionData(getLastRevision());
}

export function getCurrent() {
   return getRevisionData(getCurrentRevision());
}

export function save(title, stanzas, revision) {
    const currentRevisionIdx = getCurrentRevisionIdx();
    const currentRevision = getCurrentRevision();

    // if changes were made after last data read
    if (currentRevision > revision) {
        // save anyway ????
    }

    const newRevision = Date.now();
    const newRvIndex = Number.isInteger(currentRevisionIdx) ? currentRevisionIdx + 1 : 0;

    let revisions = getRevisions();
    if (!revisions || !revisions.length) {
        revisions = [];
    }

    if (currentRevision && currentRevision !== getLastRevision()) {
        deleteRevisions(revisions, newRvIndex, revisions.length - newRvIndex);
    }

    if (revisions.length === MAX_HISTORY_SIZE) {
        if (newRvIndex >= revisions.length / 2) {
            deleteRevisions(revisions, 0, 1);
        } else {
            deleteRevisions(revisions, revisions.length - 1, 1);
        }
    }

    revisions = push(revisions, revisions.length, newRevision);
    setRevisions(revisions);
    setCurrentRevisionIdx(newRvIndex);
    setRevisionData(newRevision, { stanzas, title });
}

export function back() {
    const currentRevisionIdx = getCurrentRevisionIdx();
    if (currentRevisionIdx > 0) {
        setCurrentRevisionIdx(currentRevisionIdx - 1);
    }
}

export function next() {
    let revisions = getRevisions();
    if (!revisions || !revisions.length) {
        return;
    }
    const currentRevisionIdx = getCurrentRevisionIdx();
    if (currentRevisionIdx < revisions.length - 1) {
        setCurrentRevisionIdx(currentRevisionIdx + 1);
    }
}

export function getCurrentRevision() {
    let revisions = getRevisions();
    if (!revisions || !revisions.length) {
        return null;
    }

    let currentRevisionIdx = getCurrentRevisionIdx();
    if (!Number.isInteger(currentRevisionIdx)) {
        currentRevisionIdx = revisions.length - 1;
        localStorage.setItem(currentRevisionIdxKey, currentRevisionIdx);
    }

    return revisions[currentRevisionIdx];
}

function deleteRevisions(revisions, fromIndex, number) {
    const deleted = revisions.splice(fromIndex, number);
    for (let rv of deleted) {
        removeRevisionData(rv);
    }
}

function getCurrentRevisionIdx() {
    return parseInt(localStorage.getItem(currentRevisionIdxKey), 10);
}

function setCurrentRevisionIdx(value) {
    return localStorage.setItem(currentRevisionIdxKey, value);
}

function getLastRevision() {
    let revisions = getRevisions();
    if (!revisions || !revisions.length) {
        return null;
    }
    return revisions[revisions.length - 1];
}

function getRevisions() {
    return JSON.parse(localStorage.getItem(revisionsKey));
}

function setRevisions(data) {
    localStorage.setItem(revisionsKey, JSON.stringify(data));
}

function getRevisionData(rv) {
    if (!rv) {
        return null;
    }
    let value = localStorage.getItem(rv);
    return {
        data: JSON.parse(value),
        revision: rv
    };
}

function setRevisionData(rv, data) {
    localStorage.setItem(rv, JSON.stringify(data));
}

function removeRevisionData(rv) {
    localStorage.removeItem(rv);
}

function clearHistory() {
    let revisions = getRevisions();
    if (revisions) {
        for (let rv of revisions) {
            removeRevisionData(rv);
        }
    }
    localStorage.removeItem(revisionsKey);
    localStorage.removeItem(currentRevisionIdxKey);
}

