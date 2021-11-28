<script>
    import { createEventDispatcher, afterUpdate } from 'svelte';
    import IconButton from './IconButton.svelte';
    import Word from './Word.svelte';
    import { getRhymes } from './api/rhymes';
    import { clickOutside } from './actions/clickOutside';
    import {
        isPunctuationMark,
        getPunctuation,
        parseStanzas,
    } from './common/strings';
    import { PoemDataStore } from './data/PoemStore';
    import Tooltip from './Tooltip.svelte';
    import * as rhymesStorage from './data/rhymesStorage';
    import { isPortraitOrientation } from './common/dom';

    export let stanzas = [];
    export let title;
    export let stanzaIndex = -1;
    export let lineIndex = -1;
    export let wordIndex = -1;

    const dispatch = createEventDispatcher();

    const titlePlaceholder = 'Введите название';
    const addLineTitle = 'Добавить строку';
    const removeLineTitle = 'Удалить строку';
    const getRhymeTitle = 'Подобрать рифму';
    const chooseTargetTooltip =
        'Выберите слово, к которому нужно подобрать рифму';
    const chooseRhymeTooltip = 'Выберите рифму из списка';
    const chooseStressTooltip = 'Укажите ударение';
    const noRhymesTooltip = 'Рифмы не найдены';
    const noRhymes = 'Не найдено';

    let titleInput;
    let chooseWord = false;
    let targetLineIndex = -1;
    let targetStanzaIndex = -1;
    let chosenLineIndex = -1;
    let chosenWordIndex = -1;
    let wordPos = 0;

    let rhymesGot = false;
    let chooseStress = false;
    let loadingRhymes = false;
    let rhymes;
    let selectedRhyme;
    let tooltipText;
    let rhymesListElem;

    const rhymesPreloaded = {};

    const store = new PoemDataStore(stanzas);

    store.subscribe((value) => {
        stanzas = value;
    });

    store.subscribeStanzaIndex((value) => {
        // console.trace()
        console.log('Sub st', value);
        stanzaIndex = value;
    });

    store.subscribeLineIndex((value) => {
        // console.trace()
        console.log('Sub l', value);
        lineIndex = value;
    });

    store.subscribeWordIndex((value) => {
        // console.log(value)
        // console.trace()
        console.log('Sub w', value);
        wordIndex = value;
    });

    const sendChangedEvent = () => {
        dispatch('changed', { stanzas: store.getStanzas(), title });
    };

    const onFocus = (s, l, w) => {
        if (chooseWord) {
            // if within this stanza update selection
            if (s === targetStanzaIndex) {
                const wordIndForRhyme = findWordForRhyme(s, l).w;
                chosenLineIndex = l;
                chosenWordIndex = wordIndForRhyme;
            } else {
                // else update target line to get rhymes to
                targetLineIndex = l;
                findChosenLineIndex(l, store.getStanza(s).length);
                findChosenWordIndex(s);
            }
        }
        store.setIndexes(s, l, w);
        console.log('Foc', store.getWord());
    };

    const onRemoveBeforeWord = (e, s, l, w) => {
        store.setIndexes(s, l, w);

        const { word } = e.detail;
        updateWord(word);

        // if first word
        if (w === 0) {
            if (l > 0) {
                // if not first line merge lines
                mergeLines(s, l);
            } else if (s > 0) {
                // if first line merge stanzas
                mergeStanzas(s);
            }
        } else {
            // merge words if not punctuation
            mergeWords(s, l, w);
        }

        sendChangedEvent();
    };

    const onRemoveAfterWord = (s, l, w) => {
        store.setIndexes(s, l, w);
        if (store.isLastLineInStanza(l, s)) {
            mergeStanzas(s + 1);
        }

        sendChangedEvent();
    };

    const onRemoveWord = (s, l, w) => {
        if (isChoosing()) {
            return;
        }

        // store.setIndexes(s, l, w);

        const words = store.getLine(l, s);

        store.removeWord(w, l, s);
        if (words) {
            if (!words.length) {
                store.setIndexes(s, l, w);
                onRemoveLine(s, l, false);
            } else {
                //???
                onBackWord(s, l, w);
            }
        }

        sendChangedEvent();
    };

    const onRemoveLine = (s, l, needUpdate = true) => {
        if (isChoosing()) {
            return;
        }

        store.setIndexes(s, l);
        store.removeLine();
        if (!store.getStanza().length) {
            store.removeStanza();
            if (s > 0) {
                store.setStanzaIndex(--s);
                store.setLineIndex(store.getStanza().length - 1);
            }
        }
        store.setWordIndex(store.getLine().length - 1);
        wordPos = store.getWord().length;

        console.log(stanzaIndex, lineIndex, wordIndex);
        if (needUpdate) {
            sendChangedEvent();
        }
    };

    const onEnter = (e, s, l, w) => {
        wordPos = 0;
        store.setIndexes(s, l, w);

        const { word, pos } = e.detail;
        store.setWord(word, w, l, s);

        //if enter after word
        if (pos === word.length) {
            // if the last word in line insert line after
            if (store.isLastWordInLine(w, l, s)) {
                onAddLine(s, l, [''], false);
            } else {
                // insert line, this word in first part
                splitLine(s, l, w + 1);
            }
        } else if (pos === 0) {
            // if enter before word
            // if first word insert line before
            if (w === 0) {
                onAddLine(s, l - 1, [''], false);
            } else {
                // insert line, this word in second part
                splitLine(s, l, w);
            }
        } else {
            // enter within word - split word into 2, then split line into 2
            splitWord(s, l, w, pos);
            splitLine(s, l, w + 1);
        }

        sendChangedEvent();
    };

    const onSpace = (e, s, l, w) => {
        wordPos = 0;
        store.setIndexes(s, l, w);
        const { word, pos } = e.detail;
        updateWord(word);

        if (!word) {
            return;
        }
        if (pos === word.length) {
            // insert word after this
            store.addWord('');
        } else if (pos === 0) {
            // insert word before this
            store.addWord('', w - 1);
        } else {
            // space within word - split into 2
            splitWord(s, l, w, pos);
        }

        sendChangedEvent();
    };

    // TODO: split word
    const onPunct = (e, s, l, w) => {
        store.setIndexes(s, l, w);

        const word = e.detail.word;
        updateWord(word);
        store.addWord(e.detail.sign);
        store.addWord('');

        sendChangedEvent();
    };

    const onPaste = (e, s, l, w) => {
        const content = e.detail.content;
        validateWord(content, s, l, w);
    };

    // needed for some mobile cases when standard key codes (space, punct) are not available and for copy text
    // TODO: finish split logic for all!!!!!
    // TODO: fix history!!!!!
    const validateWord = (word, s, l, w) => {
        const parsed = parseStanzas(word);
        console.log('PARSED', word, parsed);

        const first = parsed[0];

        // if line
        if (Array.isArray(parsed)) {
            // if stanza
            if (parsed.length > 0 && Array.isArray(first)) {
                // if stanzas
                if (first.length > 0 && Array.isArray(first[0])) {
                    for (let i = 0; i < parsed.length; i++) {
                        onAddStanza(parsed[i], s - 1 + i);
                    }
                } else {
                    // if stanza
                    onAddStanza(parsed, s - 1);
                }
            } else {
                // if line
                onAddLine(s, l, parsed);
            }
        } else {
            if (parsed.lines) {
                if (store.isLastWordInLine(w) || w === 0) {
                    onRemoveWord(s, l, w);
                    const startWInd = w === 0 ? l - 1 : l;
                    for (let i = 0; i < parsed.lines.length; i++) {
                        onAddLine(s, startWInd + i, parsed.lines[i], false);
                    }
                } else {
                    splitLine(s, l, w);
                    const rmLine = store.getLine(l + 1, s);
                    store.removeLine(l + 1, s);
                    onAddLine(s, l, parsed.lines[0]); // norm?
                    mergeLines(s, l + 1);

                    for (let i = 1; i < parsed.lines.length; i++) {
                        onAddLine(s, l + i, parsed.lines[i], false);
                    }
                    onAddLine(s, l + parsed.lines.length, rmLine, false);
                }
            } else if (parsed.words) {
                // if words
                onRemoveWord(s, l, w);
                for (let i = 0; i < parsed.words.length; i++) {
                    store.addWord(parsed.words[i], w - 1 + i, l, s);
                }
            } else {
                // if word
                updateWord(word, w, l, s);
            }
        }

        // if (word.indexOf(' ') >= 0) {
        //     word = word.replace(' ', '');
        //     console.log('SPACE', word, word.length);
        //     onSpace({ detail: { word, pos } }, s, l, w);
        // }
        // const punct = getPunctuation(word);
        // if (punct) {
        //     console.log('PUNCT', punct);
        //     word = word.replace(punct, '');
        //     onPunct({ detail: { word, sign: punct, pos } }, s, l, w);
        // }
    };

    const onEditFinished = (e, s, l, w) => {
        const { word } = e.detail;
        console.log('EDIT', word, store.getWord(w, l, s));
        if (!word) {
            onRemoveWord(s, l, w);
        } else if (
            typeof store.getWord(w, l, s, true) !== 'undefined' &&
            word !== store.getWord(w, l, s)
        ) {
            validateWord(word, s, l, w);
            store.setIndexes(s, l, w);
            updateWord(word);
            sendChangedEvent();
        }
    };

    const onChange = (e, s, l, w) => {
        validateWord(e.detail.word, s, l, w);
    };

    const onNextWord = (s, l, w) => {
        console.log('NEXT', store.getWord(w, l, s));
        store.setIndexes(s, l, w);
        if (!store.isLastWordInLine(w, l, s)) {
            store.setWordIndex(++w);
        } else {
            onNextLine(s, l, w);
        }
        wordPos = 0;
    };

    const onNextLine = async (s, l, w, sameWordIndex) => {
        store.setIndexes(s, l, w);
        if (!store.isLastLineInStanza(l, s)) {
            store.setLineIndex(++l);
            if (sameWordIndex) {
                setWordIndexInLine(w);
            } else {
                store.setWordIndex(0);
            }
        } else {
            onNextStanza(s, l, w);
        }
        wordPos = 0;
    };

    const onNextStanza = (s, l, w) => {
        store.setIndexes(s, l, w);
        if (s < store.getSize() - 1) {
            store.setStanzaIndex(++s);
            store.setLineIndex(0);
            setWordIndexInLine(w);
            wordPos = 0;
        }
    };

    const onBackWord = (s, l, w) => {
        console.log('back word', s, l, w);
        store.setIndexes(s, l, w);
        if (w > 0) {
            store.setWordIndex(--w);
        } else {
            onBackLine(s, l, w);
        }
        console.log('BACK');
        wordPos = store.getWord().length;
    };

    const setWordIndexInLine = (w) => {
        const maxWordInd = store.getLineSize() - 1;
        store.setWordIndex(w > maxWordInd ? maxWordInd : w);
    };

    const onBackLine = (s, l, w, sameWordIndex) => {
        console.log('BACK l');
        store.setIndexes(s, l, w);
        if (l > 0) {
            store.setLineIndex(--l);
            if (sameWordIndex) {
                setWordIndexInLine(w);
            } else {
                store.setWordIndex(store.getLineSize() - 1);
            }
            stanzas = store.getStanzas();
        } else {
            onBackStanza(s, l, w);
        }
        wordPos = store.getWord().length;
    };

    const onBackStanza = (s, l, w) => {
        console.log('BACK s');
        store.setIndexes(s, l, w);
        if (s > 0) {
            store.setStanzaIndex(--s);
            store.setLineIndex(store.getStanzaSize() - 1);
            setWordIndexInLine(w);
        }
        wordPos = store.getWord().length;
    };

    // TODO: debounce
    // TODO: set correct index in next line
    const onUp = (s, l, w) => {
        store.setIndexes(s, l, w);
        onBackLine(s, l, w, true);
    };

    const onDown = (s, l, w) => {
        store.setIndexes(s, l, w);
        onNextLine(s, l, w, true);
    };

    const onAddStanza = (lines, s) => {
        store.setIndexes(s);
        const stanza = store.getStanza(s > 0 ? s - 1 : 0);
        if (stanza && stanza.length && stanza[0]) {
            console.log('add st', stanza[0]);
            store.addStanza(lines, s);
        }

        sendChangedEvent();
    };

    const updateWord = (
        word,
        w = wordIndex,
        l = lineIndex,
        s = stanzaIndex
    ) => {
        console.log('upd', word);
        if (word) {
            console.log('set', word);
            store.setWord(word, w, l, s);
        } else {
            store.removeWord();
            if (w > 0) {
                store.setWordIndex(--w);
            }
        }
    };

    // change args order
    const onAddLine = async (
        s = stanzaIndex,
        l,
        newLine,
        needUpdate = true
    ) => {
        if (isChoosing()) {
            return;
        }

        store.setIndexes(s, l);
        const line = store.getLine();
        const isNewEmpty = store.isLineEmpty(newLine);

        // if current line is empty
        if (l >= 0 && store.isLineEmpty(line)) {
            // remove this line
            if (isNewEmpty) {
                store.removeLine();
            }

            if (!store.getStanza(s).length) {
                // if stanza is empty, remove it
                store.removeStanza(s);
                store.setStanzaIndex(stanzaIndex);
                store.setLineIndex(0);
            } else if (l === store.getStanza().length) {
                // if the last line, add stanza
                store.setLineIndex(0);
                onAddStanza([['']], s);
            } else if (l === 0) {
                // if the first line, add stanza
                onAddStanza([['']], s - 1);
            } else if (isNewEmpty) {
                // if new line is also empty: 2 empty lines => new stanza
                splitStanza(s, l);
            }
        } else {
            store.addLine(isNewEmpty ? [''] : newLine, l, s);
            store.setWordIndex(0);
        }
        const res = findWordForRhyme(stanzaIndex, lineIndex);
        const word = store.getWord(res.w, res.l, s);
        const preloaded = rhymesStorage.getRhymes(word);
        if (preloaded.needToLoad) {
            console.log('LOAD', res.w, res.l, word);
            let response = await getRhymes(word);
            if (response.data) {
                rhymesStorage.setRhymes(word, response.data);
            }
        }

        if (needUpdate) {
            sendChangedEvent();
        }
    };

    const splitStanza = (s, l) => {
        const stanza = store.getStanza(s);
        const stanza1 = stanza.slice(0, l);
        const stanza2 = stanza.slice(l);
        onAddStanza(stanza1, s);
        onAddStanza([['']], s + 1); // bad pattern
        onAddStanza(stanza2, s + 2);
        store.removeStanza(s);
        store.setIndexes(s + 1, 0, 0);
    };

    const mergeStanzas = (s) => {
        const stanza1 = store.getStanza(s - 1);
        const stanza2 = store.getStanza(s);
        const stanza = stanza1.concat(stanza2);
        onAddStanza(stanza, s - 1);
        store.removeStanza(s - 1);
        store.removeStanza(s);
        store.setIndexes(s - 1, stanza1.length, 0);
    };

    const splitLine = (s, l, w) => {
        const line = store.getLine(l, s);
        const line1 = line.slice(0, w);
        const line2 = line.slice(w, line.length ? line.length : 1);
        console.log('Bef', line1, line2, store.getStanza());
        onAddLine(s, l, line1, false);
        console.log('Aft l1', store.getStanza(), store.getLine());
        onAddLine(s, l + 1, line2, false);
        console.log('Aft l2', store.getStanza());
        store.removeLine(l);
        console.log('Set idsx', s, l + 1, 0);
        store.setIndexes(s, l + 1, 0);
        wordPos = 0;
        console.log(line1, line2, store.getStanza(), store.getLine());
    };

    const mergeLines = (s, l) => {
        let line1 = store.getLine(l - 1, s);
        let line2 = store.getLine(l, s);
        let line = line1.concat(line2);
        if (store.isLineEmpty(line1)) {
            line = line2;
            line1 = [];
        } else if (store.isLineEmpty(line2)) {
            line = line1;
        }
        store.removeLine(l - 1, s);
        store.removeLine(l - 1, s);
        onAddLine(s, l - 2, line, false);
        store.setIndexes(s, l - 1, line1.length);
        wordPos = 0;
    };

    const splitWord = (s, l, w, pos) => {
        wordPos = 0;
        console.log('split 0', s, l, w, store.getWord());

        const word = store.getWord(w, l, s);
        const word1 = word.slice(0, pos);
        const word2 = word.slice(pos, word.length ? word.length : 1);
        store.addWord(word1, w, l, s);
        store.addWord(word2, w + 1, l, s);
        store.removeWord(w);
        store.setIndexes(s, l, w + 1);
    };

    const mergeWords = (s, l, w) => {
        const word1 = store.getWord(w - 1, l, s);
        const word2 = store.getWord(w, l, s);

        // const isPunct1 = isPunctuationMark(word1);
        // const isPunct2 = isPunctuationMark(word2);
        // const isHyphen1 = isHyphen(word1);
        // const isHyphen2 = isHyphen(word2);
        //
        // // optimize? p2 && (h2 || p1) || p1 && (h1 || p2)
        // if (isPunct2 && (!isHyphen2 || isPunct1)) {
        //     return;
        // }
        // if (isPunct1 && (!isHyphen1 || isPunct2)) {
        //     return;
        // }

        const word = word1.concat(word2);
        store.addWord(word, w, l, s);
        store.removeWord(w, l, s);
        store.removeWord(w - 1, l, s);
        store.setIndexes(s, l, w - 1);

        wordPos = word1.length;
    };

    const displayRhymes = (data) => {
        rhymes = data;
        rhymesGot = true;
        loadingRhymes = false;
        chooseWord = false;
        if (rhymes.length) {
            selectedRhyme = rhymes[0].toLowerCase();
            console.log('SEL', selectedRhyme);
        }
    };

    const onGetRhymes = async (stressPos) => {
        const word = store.getWord(
            chosenWordIndex,
            chosenLineIndex,
            stanzaIndex
        );

        const preloaded = rhymesStorage.getRhymes(word);
        if (!preloaded.needToLoad) {
            displayRhymes(preloaded.rhymes);
        } else {
            loadingRhymes = true;
            const response = await getRhymes(word, stressPos);
            if (response.data) {
                response.data = response.data
                    .map((r) => r.toLowerCase())
                    .sort();
                rhymesStorage.setRhymes(word, response.data, !!stressPos);
                displayRhymes(response.data);
            } else if (response.error === 'Stress needed') {
                loadingRhymes = false;
                chooseWord = false;
                chooseStress = true;
                wordPos = 0;
            } else {
                loadingRhymes = false;
                console.log('Ошибка: ' + response.status);
            }
        }
    };

    const findChosenLineIndex = (
        l = lineIndex,
        stanzaLength = store.getStanza().length
    ) => {
        // get line to get rhyme to
        if (l > 1) {
            chosenLineIndex = l - 2;
        } else if (l > 0) {
            chosenLineIndex = l - 1;
        } else if (l < stanzaLength - 2) {
            chosenLineIndex = l + 2;
        } else if (l < stanzaLength - 1) {
            chosenLineIndex = l + 1;
        }
        // TODO: finish odd logic
        // // for even lines
        // if (l % 2 === 0) {
        //     // if there are previous lines, go 2 lines upper
        //     if (l > 1) {
        //         chosenLineIndex = l - 2;
        //     } else if (stanzaLength === 1) { // if the first line and only 2 lines, go to the second
        //         chosenLineIndex = l - 1;
        //     } else {} // if the first line and only 1 line, nothing to do
        // } else {  // for odd lines
        //
        // }
    };

    const findChosenWordIndex = (s = stanzaIndex) => {
        let w = store.getLine(chosenLineIndex, s).length;
        let l = chosenLineIndex;

        const lw = findWordForRhyme(s, l, w);

        chosenLineIndex = lw.l;
        chosenWordIndex = lw.w;
    };

    const findWordForRhyme = (s, l, w) => {
        let isPunct = false;

        if (!w) {
            w = store.getLine(l, s).length;
        }

        // find word
        do {
            const word = store.getWord(--w, l, s);
            isPunct = isPunctuationMark(word);

            // if there's no words in line, go to previous line
            if (w < 0) {
                w = 0;
                // if the first line in stanza skip
                if (l === 0) {
                    return;
                }
                l--;
            }
        } while (isPunct && w > 0);

        return {
            w,
            l,
        };
    };

    const onChooseBefore = () => {
        if (chosenLineIndex > 0) {
            //TODO: forbid choose target line
            //if (chosenLineIndex == targetLineIndex)
            onBackLine(stanzaIndex, chosenLineIndex, chosenWordIndex);
            chosenLineIndex = lineIndex;
            findChosenWordIndex();
        }
    };

    const onChooseAfter = () => {
        if (!store.isLastLineInStanza(chosenLineIndex, stanzaIndex)) {
            onNextLine(stanzaIndex, chosenLineIndex, chosenWordIndex);
            chosenLineIndex = lineIndex;
            findChosenWordIndex();
        }
    };

    const onBulbClick = (s, l) => {
        if (isChoosing()) {
            resetChoice();
        } else if (store.getLastWord(l, s)) {
            store.setIndexes(s, l);
            chooseWord = !(chooseWord && l === targetLineIndex);
            wordPos = 0;
            targetStanzaIndex = s;
            targetLineIndex = l;

            // set chosen rhyme line
            findChosenLineIndex();
            findChosenWordIndex();
        }
    };

    const onClickOutsideStanza = (s) => {
        if (s === targetStanzaIndex) {
            resetChoice();
        }
    };

    const onLineClick = (s, l) => {
        if (chooseWord) {
            const wordIndForRhyme = findWordForRhyme(s, l).w;
            chosenLineIndex = l;
            chosenWordIndex = wordIndForRhyme;
        }
        if (!isChoosing()) {
            store.setStanzaIndex(s);
            store.setLineIndex(l);
            store.setWordIndex(store.getLineSize(l) - 1);
            wordPos = store.getLastWord(l, s).length;
        }
    };

    const onConfirmRhymes = () => {
        // TODO: move to states machine-like maybe??
        console.log('confirm poem', rhymesGot, selectedRhyme);
        if (rhymesGot) {
            if (rhymes.length) {
                store.addWord(
                    selectedRhyme,
                    store.getLineSize(targetLineIndex) - 1,
                    targetLineIndex
                );
                sendChangedEvent();
            }
            resetChoice();
            store.setIndexes(stanzaIndex, targetLineIndex, -1);
        } else if (chooseStress) {
            onGetRhymes(wordPos);
        } else {
            onGetRhymes();
        }
        chooseStress = false;
    };

    const onRejectRhymes = () => {
        resetChoice();
    };

    const resetChoice = () => {
        chooseWord = false;
        rhymesGot = false;
        chooseStress = false;
        loadingRhymes = false;

        store.resetIndexes();
        wordPos = 0;

        rhymes = [];
        selectedRhyme = '';
        targetStanzaIndex = -1;
    };

    $: {
        if (rhymesGot) {
            if (rhymes.length) {
                tooltipText = chooseRhymeTooltip;
            } else {
                tooltipText = noRhymesTooltip;
            }
        } else {
            if (chooseStress) {
                tooltipText = chooseStressTooltip;
            } else {
                tooltipText = chooseTargetTooltip;
            }
        }
    }

    document.addEventListener('keydown', (e) => {
        if (isChoosing()) {
            if (e.key === 'Escape') {
                e.preventDefault();
                onRejectRhymes();
            } else if (e.key === 'Enter') {
                e.preventDefault();
                onConfirmRhymes();
            } else if (chooseWord) {
                e.preventDefault();
                if (e.key === 'ArrowLeft') {
                    onChooseBefore();
                } else if (e.key === 'ArrowRight') {
                    onChooseAfter();
                } else if (e.key === 'ArrowUp') {
                    onChooseBefore();
                } else if (e.key === 'ArrowDown') {
                    onChooseAfter();
                }
            }
        }
    });

    afterUpdate(() => {
        store.setStanzas(stanzas);
    });

    const isChoosing = () => {
        return chooseWord || rhymesGot || chooseStress || loadingRhymes;
    };

    $: isLineChosenForRhymes = (s, l) => {
        return isChoosing() && s === targetStanzaIndex && l === targetLineIndex;
    };

    $: isWordChosenForRhymes = (s, l, w) => {
        const indexesEquals =
            s === targetStanzaIndex &&
            l === chosenLineIndex &&
            w === chosenWordIndex;
        return isChoosing() && indexesEquals;
    };

    $: isNothingSelected = () => {
        return stanzaIndex < 0 || lineIndex < 0 || wordIndex < 0;
    };

    $: isWordSelected = (s, l, w) => {
        return (
            !isNothingSelected() &&
            s === stanzaIndex &&
            l === lineIndex &&
            w === wordIndex
        );
    };

    $: if (rhymesListElem) {
        rhymesListElem.focus();
        // TODO: for some reason 'selectedRhyme' is not set when preloaded
        if (rhymes.length) {
            // selectedRhyme = rhymes[0].toLowerCase();
            // rhymesListElem.value = rhymes[0].toLowerCase();
        }
    }

    $: isFocused = (s, l, w) => {
        return (
            isWordChosenForRhymes(s, l, w) ||
            (!isChoosing() && isWordSelected(s, l, w))
        );
    };

    const onInputTitle = () => {
        title = titleInput.textContent;
        sendChangedEvent();
    };

    $: lineBarBtnSz = isPortraitOrientation() ? '15' : '18';
</script>

<div class="poem">
    <h2
        contenteditable
        spellcheck="false"
        bind:this={titleInput}
        on:input={onInputTitle}
        class:empty={!title}
        {title}
    >
        {title}
    </h2>
    {#each stanzas as lines, s}
        <div
            class="stanza"
            use:clickOutside
            on:clickOutside={() => onClickOutsideStanza(s)}
        >
            {#each lines as words, l}
                <div
                    class="line"
                    on:click|preventDefault={() => onLineClick(s, l)}
                >
                    <div class="words">
                        {#each words as word, w}
                            <Word
                                {word}
                                bind:pos={wordPos}
                                chosen={isWordChosenForRhymes(s, l, w)}
                                focused={isFocused(s, l, w)}
                                editable={!isChoosing() &&
                                    !isWordChosenForRhymes(s, l, w)}
                                chooseStress={chooseStress &&
                                    isWordChosenForRhymes(s, l, w)}
                                on:focus={() => onFocus(s, l, w)}
                                on:remove={() => onRemoveWord(s, l, w)}
                                on:remove-before={(e) =>
                                    onRemoveBeforeWord(e, s, l, w)}
                                on:remove-after={(e) =>
                                    onRemoveAfterWord(e, s, l, w)}
                                on:enter={(e) => onEnter(e, s, l, w)}
                                on:space={(e) => onSpace(e, s, l, w)}
                                on:punct={(e) => onPunct(e, s, l, w)}
                                on:paste={(e) => onPaste(e, s, l, w)}
                                on:back={(e) => onBackWord(s, l, w, e)}
                                on:next={(e) => onNextWord(s, l, w, e)}
                                on:up={(e) => onUp(s, l, w, e)}
                                on:down={(e) => onDown(s, l, w, e)}
                                on:change={(e) => onChange(e, s, l, w)}
                                on:edit-finished={(e) =>
                                    onEditFinished(e, s, l, w)}
                            />
                        {/each}
                        {#if rhymesGot && l === targetLineIndex && s === stanzaIndex}
                            <select
                                bind:value={selectedRhyme}
                                bind:this={rhymesListElem}
                            >
                                {#if rhymes.length}
                                    {#each rhymes as rhyme}
                                        <option>{rhyme.toLowerCase()}</option>
                                    {/each}
                                {:else}
                                    <option>{noRhymes}</option>
                                {/if}
                            </select>
                        {/if}
                        <div class="right-menu">
                            <div class="bulb-container">
                                <div
                                    class="menu-button"
                                    class:active={isLineChosenForRhymes(s, l)}
                                >
                                    <IconButton
                                        icon="bulb"
                                        iconSize={lineBarBtnSz}
                                        padding="4"
                                        opacity={isLineChosenForRhymes(s, l)
                                            ? 1
                                            : 0.2}
                                        title={getRhymeTitle}
                                        on:click={() => onBulbClick(s, l)}
                                    />
                                </div>
                            </div>
                            <Tooltip
                                open={isLineChosenForRhymes(s, l)}
                                text={tooltipText}
                                up={s > 0 || l > 1}
                                showConfirm={!rhymesGot || rhymes.length}
                                on:reject={onRejectRhymes}
                                on:confirm={onConfirmRhymes}
                            />
                            <div class="menu-button">
                                <IconButton
                                    icon="cross"
                                    iconSize={lineBarBtnSz}
                                    padding="4"
                                    title={removeLineTitle}
                                    on:click={() => onRemoveLine(s, l)}
                                />
                            </div>
                            <div class="menu-button">
                                <IconButton
                                    icon="plus"
                                    iconSize={lineBarBtnSz}
                                    padding="4"
                                    title={addLineTitle}
                                    on:click={() => onAddLine(s, l)}
                                />
                            </div>
                        </div>
                    </div>
                </div>
            {/each}
        </div>
    {/each}
</div>

<style lang="scss">
    @import './styles/_mixins';

    .poem {
        display: flex;
        min-width: 70%;
        max-width: 100%;
        width: fit-content;
        width: -moz-max-content;
        flex-direction: column;
        margin-left: auto;
        margin-right: auto;
        padding: 0.5rem;
        margin-bottom: 0.25rem;

        & h2 {
            font-size: larger;
            margin: 0.5rem 0 0.5rem 0.75rem;
            &.empty {
                color: #cccccc;
            }
        }
    }

    .stanza {
        width: 100%;
        max-width: calc(100% - 1rem);
        margin-bottom: 1rem;
        padding: 0.5rem;

        &:hover,
        &:focus-within {
            background-color: #f5f5f5;
        }
    }

    .line {
        display: flex;
        align-items: center;
        justify-content: space-between;
        line-height: 2rem;
        width: 100%;

        & .words {
            //display: flex;
            //align-items: center;
            padding: 0.25rem 0.5rem;
            width: 100%;
            line-height: 1.5rem;
        }

        .right-menu {
            position: relative;
            display: flex;
            align-items: center;
            float: right;
            margin-left: 1rem;

            & .menu-button {
                opacity: 0;

                &.active {
                    opacity: 1;
                }
            }
        }

        &:hover,
        &:focus,
        &:focus-within {
            background-color: #e5e5e5;

            .right-menu {
                & .menu-button {
                    opacity: 1;
                }
            }
        }
    }

    .bulb-container {
        &:hover + .tooltip {
            display: block;
        }
    }

    @include sm {
        h2 {
            margin-left: 2rem !important;
        }
    }

    @include lg {
        .poem {
            padding: 1rem;
            padding-bottom: 3rem;
        }

        h2 {
            margin: 1rem 2rem !important;
            font-size: x-large !important;
        }
    }

    @include portrait {
        .poem {
            padding: 0.5rem;
            margin-bottom: 0.25rem !important;
        }

        .stanza {
            padding: 0.5rem 0.25rem;
        }

        .line {
            line-height: 1rem;
        }

        .words {
            padding: 0.25rem 0.25rem !important;
            line-height: 1.25rem;
        }

        & .menu-button {
            width: 1.25rem;
        }
    }
</style>
