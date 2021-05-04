<script>
    import { createEventDispatcher } from 'svelte';
    import IconButton from "./IconButton.svelte";
    import Word from "./Word.svelte";
    import { push, remove } from "./common/arrays";
    import { getRhymes } from "./api/rhymes";

    export let stanzas = [];

    export let stanzaIndex = -1;
    export let lineIndex = -1;
    export let wordIndex = -1;
    let wordPos;

    const dispatch = createEventDispatcher();

    const title = 'Стихотворение';
    const addLineTitle = 'Добавить строку';
    const removeLineTitle = 'Удалить строку';
    const getRhymeTitle = 'Подобрать рифму';

    //TODO: set correct word pos

    const onFocus = (s, l, w) => {
        setIndexes(s, l, w);
        console.log('foc')
    };

    const onBlur = (e, s, l, w) => {
        // const word = e.detail.word;
        // if (!word) {
        //     console.log('rm in word')
        //     removeWord(w, l, s);
        // }
        // const stanza = getStanza(s);
        // const line = getLine(s, l);
        //
        // if (!line || !line.length || !getWord(0, l, s)) {
        //     removeLine();
        // }
        // if (!stanza || !stanza.length || !getLine(0, s)) {
        //     removeStanza();
        // }
    };

    const onRemoveBeforeWord = (e, s, l, w) => {
        setIndexes(s, l, w);

        const { word, punct } = e.detail;
        updateWord(word);

        // if first word
        if (w === 0) {
            if (l > 0) { // if not first line merge lines
                mergeLines(s, l);
            } else if (s > 0) { // if first line merge stanzas
                mergeStanzas(s);
            }
        } else if (!punct) { // merge words if not punctuation
            mergeWords(s, l, w);
        }
    };

    const onRemoveAfterWord = (s, l, w) => {
        setIndexes(s, l, w);
        const stanza = getStanza();
        if (l === stanza.length - 1) {
            mergeStanzas(s + 1);
        }
    };

    const onRemoveWord = (s, l, w) => {
        setIndexes(s, l, w);
        const words = getLine();

        removeWord();

        if (!words.length) {
            console.log('rm line - empty on rem word', words)
            onRemoveLine(s, l);
        } else if (wordIndex === words.length) { //???
            onBackWord(s, l, w);
        }
    };

    const onRemoveLine = (s, l) => {
        setIndexes(s, l);
        console.log('rm line', getStanza(), l)
        removeLine();
        if (!getStanza().length) {
            removeStanza();
        }
    };

    const onEnter = (e, s, l, w) => {
        setIndexes(s, l, w);

        const words = getLine();
        const { word, pos } = e.detail;
        setWord(word, w, l, s);

        //if enter after word
        if (pos === word.length) {
            // if the last word in line insert line after
            if (w === words.length - 1) {
                onAddLine(s, l, [ '' ]);
            } else {
                // insert line, this word in first part
                splitLine(s, l, w + 1);
            }
        } else if (pos === 0) { // if enter before word
            // if first word insert line before
            if (w === 0) {
                onAddLine(s, l - 1, [ '' ]);
            } else {
                // insert line, this word in second part
                splitLine(s, l, w);
            }
        } else { // enter within word - split word into 2, then split line into 2
            splitWord(s, l, w, pos);
            splitLine(s, l, w + 1);
        }
    };

    const onSpace = (e, s, l, w) => {
        setIndexes(s, l, w);
        const words = getLine();
        const { word, pos } = e.detail;
        updateWord(word);

        console.log('space', words, word, wordIndex)

        if (!word) {
            return;
        }
        if (pos === word.length) {
            // insert word after this
            addWord('');
        } else if (pos === 0) {
            // insert word before this
            addWord('', w - 1);
        } else { // space within word - split into 2
            splitWord(s, l, w, pos);
        }
    };

    const onPunct = (e, s, l, w) => {
        setIndexes(s, l, w);
        const word = e.detail.word;
        updateWord(word);
        addWord(e.detail.sign);
        addWord('');
    };

    const onNextWord = (s, l, w) => {
        setIndexes(s, l, w);
        const words = getLine();

        console.log('next word', w, w + 1, words)
        if (w < words.length - 1) {
            setWordIndex(++w);
        } else {
            onNextLine(s, l, w);
        }
    };

    const onNextLine = (s, l, w) => {
        setIndexes(s, l, w);
        const lines = getStanza();

        console.log('next line', lines, l)
        if (l < lines.length - 1) {
            setLineIndex(++l);
            setWordIndex(0);
        } else {
            onNextStanza(s, l, w);
        }
    };

    const onNextStanza = (s, l, w) => {
        setIndexes(s, l, w);
        if (s < stanzas.length - 1) {
            setStanzaIndex(++s);
            setLineIndex(0);
            setWordIndex(0);
            console.log('nxt')
        }
    };

    const onBackWord = (s, l, w) => {
        setIndexes(s, l, w);
        const words = getLine();

        console.log('back', w, w - 1, words)
        if (w > 0) {
            setWordIndex(--w);
        } else {
            onBackLine(s, l, w);
        }
    };

    const onBackLine = (s, l, w) => {
        setIndexes(s, l, w);
        const lines = getStanza();

        // console.log('back to line', getLine(), getLine().length - 1)
        if (l > 0) {
            setLineIndex(--l);
            setWordIndex(getLine().length - 1);
            console.log('idxs', stanzaIndex, lineIndex, wordIndex)
            stanzas = stanzas
        } else {
            onBackStanza(s, l, w);
        }
    };

    const onBackStanza = (s, l, w) => {
        setIndexes(s, l, w);
        if (s > 0) {
            setStanzaIndex(--s);
            const stanza = getStanza();

            setLineIndex(stanza.length - 1);
            const line = getLine();

            setWordIndex(line.length - 1);
            console.log('bc', stanzaIndex, lineIndex, wordIndex)
        }
    };

    const onUp = (s, l, w) => {
        setIndexes(s, l, w);
        onBackLine(s, l, w);
    };

    const onDown = (s, l, w) => {
        setIndexes(s, l, w);
        onNextLine(s, l, w);
    };

    const onAddStanza = (lines, s) => {
        setIndexes(s);
        const stanza = getStanza(s > 0 ? s - 1 : 0);
        console.log('add st', s, getStanza());

        if (stanza && stanza.length && stanza[0]) {
            addStanza(lines, s);
        }
    };

    const updateWord = (word, w = wordIndex, l = lineIndex, s = stanzaIndex) => {
        if (word) {
            console.log('add word', getLine(), word, w)
            setWord(word, w, l, s);
        } else {
            console.log('add empty word - remove', getLine(), w)
            removeWord();
            if (w > 0) {
                setWordIndex(--w);
            }
        }
    };

    // change args order
    const onAddLine = (s = stanzaIndex, l, newLine) => {
        setIndexes(s, l);
        const line = getLine();
        const isNewEmpty = isLineEmpty(newLine);
        console.log('add line', line);

        // if current line is empty
        if (l >= 0 && isLineEmpty(line)) {
            console.log('rm line', line, l)
            // remove this line
            if (isNewEmpty) {
                removeLine();
            }

            if (!getStanza(s).length) { // if stanza is empty, remove it
                removeStanza(s);
                setStanzaIndex(stanzaIndex + 1);
                setLineIndex(0);
            } else if (l === getStanza().length) { // if the last line, add stanza
                setLineIndex(0);
                onAddStanza([ [ '' ] ], s);
            } else if (l === 0) {  // if the first line, add stanza
                onAddStanza([ [ '' ] ], s - 1);
            } else if (isNewEmpty) { // if new line is also empty: 2 empty lines => new stanza
                splitStanza(s, l);
            }
        } else {
            addLine(isNewEmpty ? [ '' ] : newLine, l, s);
            setWordIndex(0);
        }
    };

    const splitStanza = (s, l) => {
        const stanza = getStanza(s);
        const stanza1 = stanza.slice(0, l);
        const stanza2 = stanza.slice(l);
        onAddStanza(stanza1, s);
        onAddStanza([ [ '' ] ], s + 1);  // bad pattern
        onAddStanza(stanza2, s + 2);
        removeStanza(s);
        setIndexes(s + 1, 0, 0);
    };

    const mergeStanzas = s => {
        const stanza1 = getStanza(s - 1);
        const stanza2 = getStanza(s);
        const stanza = stanza1.concat(stanza2);
        onAddStanza(stanza, s - 1);
        removeStanza(s - 1);
        removeStanza(s);
        setIndexes(s - 1, stanza1.length, 0);
    };

    const splitLine = (s, l, w) => {
        const line = getLine(l, s);
        const line1 = line.slice(0, w);
        const line2 = line.slice(w, line.length ? line.length : 1);
        onAddLine(s, l, line1);
        // onAddLine(s, l + 1, [ '' ]);  // bad pattern
        onAddLine(s, l + 1, line2);
        removeLine(l);
        setIndexes(s, l + 1, 0);
    };

    const mergeLines = (s, l) => {
        let line1 = getLine(l - 1, s);
        let line2 = getLine(l, s);
        let line = line1.concat(line2);
        if (isLineEmpty(line1)) {
            line = line2;
            line1 = [];
        } else if (isLineEmpty(line2)) {
            line = line1;
        }
        removeLine(l - 1, s);
        removeLine(l - 1, s);
        onAddLine(s, l - 2, line);
        setIndexes(s, l - 1, line1.length);
    };

    const splitWord = (s, l, w, pos) => {
        const word = getWord(w, l, s);
        const word1 = word.slice(0, pos);
        const word2 = word.slice(pos, word.length ? word.length : 1);
        addWord(word1, w, l, s);
        addWord(word2, w + 1, l, s);
        removeWord(w);
        setIndexes(s, l, w + 1);
        // wordPos = 0;
    };

    const mergeWords = (s, l, w) => {
        const word1 = getWord(w - 1, l, s);
        const word2 = getWord(w, l, s);
        const word = word1.concat(word2);
        addWord(word, w, l, s);
        removeWord(w, l, s);
        removeWord(w - 1, l, s);
        setIndexes(s, l, w - 1);
    };

    const onGetRhymes = async (s, l) => {
        const word = getLastWord(l, s);
        let response = await getRhymes(word);
        if (response.data) {
            const rhymes = response.data;
        } else {
            console.log("Ошибка: " + response.status);
        }
    };

    const getStanza = (s = stanzaIndex) => {
        return stanzas[s];
    };

    const setStanza = (lines, s = stanzaIndex) => {
        stanzas[s] = lines;
    };

    const addStanza = (lines = [ [ '' ] ], s = stanzaIndex) => {
        setStanzaIndex(++s);
        setIndexes(s, 0, 0);
        stanzas = push(stanzas, s, lines);
    };

    const removeStanza = (s = stanzaIndex) => {
        setStanzaIndex(s);

        // if the last stanza, set the last line of previous one
        if (s === stanzas.length - 1 && s > 0) {
            setStanzaIndex(--s);
            const stanza = getStanza(s);
            if (stanza.length) {
                setLineIndex(stanza.length - 1);
            }
        } else {  // set the first line of the next one
            setLineIndex(0);
        }

        // leave at least one stanza
        if (stanzas.length > 1) {
            remove(stanzas, s);
        } else {
            setStanza([['']], 0);
            setIndexes(0, 0, 0);
        }
    };

    const getLine = (l = lineIndex, s = stanzaIndex) => {
        const stanza = getStanza(s);
        if (stanza) {
            return stanza[l];
        }
    };

    const setLine = (words, l = lineIndex, s = stanzaIndex) => {
        const stanza = getStanza(s);
        if (stanza) {
            stanzas[s][l] = words;
        }
    };

    const addLine = (words = [ '' ], l = lineIndex, s = stanzaIndex) => {
        setLineIndex(++l);
        setStanza(push(getStanza(s), l, words));
    };

    const removeLine = (l = lineIndex, s = stanzaIndex) => {
        const stanza = getStanza(s);
        if (stanza) {
            console.log('rm ln', s, l, getStanza(s));
            remove(getStanza(s), l);
            setStanza((getStanza())); // hack
            if (l > 0) {
                setLineIndex(--l);
            }
            setWordIndex(0);
            // console.log('IDX', stanzaIndex, lineIndex, wordIndex)
        }
    };

    const isLineEmpty = line => {
        return !line || !line.length || line.length === 1 && !line[0];
    };

    const getWord = (w = wordIndex, l = lineIndex, s = stanzaIndex) => {
        const line = getLine(l, s);
        if (line) {
            return line[w];
        }
    };

    const getLastWord = (l = lineIndex, s = stanzaIndex) => {
        const line = getLine(l, s);
        if (line) {
            return line[line.length - 1];
        }
    };

    const setWord = (word, w = wordIndex, l = lineIndex, s = stanzaIndex) => {
        const line = getLine(l, s);
        if (line) {
            line[w] = word;
        }
    };

    const addWord = (word, w = wordIndex, l = lineIndex, s = stanzaIndex) => {
        setWordIndex(++w);
        setLine(push(getLine(l, s), w, word));
    };

    const removeWord = (w = wordIndex, l = lineIndex, s = stanzaIndex) => {
        const line = getLine(l, s);
        if (line) {
            remove(line, w);
            stanzas = stanzas;
        }
    };

    const setIndexes = (s, l, w) => {
        console.log('idx', s, l, w);
        setWordIndex(w);
        setLineIndex(l);
        setStanzaIndex(s);
    };

    const setWordIndex = w => {
        if (typeof w !== 'undefined') {
            wordIndex = w;
        }
    };

    const setLineIndex = l => {
        if (typeof l !== 'undefined') {
            lineIndex = l;
        }
    };

    const setStanzaIndex = s => {
        if (typeof s !== 'undefined') {
            stanzaIndex = s;
        }
    };

    const resetIndexes = () => {
        setIndexes(-1, -1, -1);
    };

</script>

<div class="poem">
    <h2 contenteditable>
        {title}
    </h2>
    {#each stanzas as lines, s}
        <div class="stanza">
            {#each lines as words, l}
                <div class="line">
                    <div>
                        {#each words as word, w}
                            <Word
                                    {word}
                                    pos={wordPos}
                                    editable={s === stanzaIndex && l === lineIndex && w === wordIndex}
                                    on:focus={() => onFocus(s, l, w)}
                                    on:blur={e => onBlur(e, s, l, w)}
                                    on:remove={() => onRemoveWord(s, l, w)}
                                    on:remove-before={e => onRemoveBeforeWord(e, s, l, w)}
                                    on:remove-after={e => onRemoveAfterWord(e, s, l, w)}
                                    on:enter={e => onEnter(e, s, l, w)}
                                    on:space={e => onSpace(e, s, l, w)}
                                    on:punct={e => onPunct(e, s, l, w)}
                                    on:back={() => onBackWord(s, l, w)}
                                    on:next={() => onNextWord(s, l, w)}
                                    on:up={() => onUp(s, l, w)}
                                    on:down={() => onDown(s, l, w)}
                                    on:editFinished/>
                        {/each}
                    </div>

<!--                    <div class="right-menu">-->
<!--                        <IconButton icon="bulb" size="18" padding="4" title={getRhymeTitle} on:click={() => onGetRhymes(s, l)}/>-->
<!--                        <IconButton icon="cross" size="14" padding="4" title={removeLineTitle} on:click={() => onRemoveLine(s, l)}/>-->
<!--                        <IconButton icon="plus" size="14"  padding="4" title={addLineTitle} on:click={() => onAddLine(s, l)}/>-->
<!--                    </div>-->
                </div>
            {/each}
        </div>
    {/each}
</div>

<style lang="scss">
    .poem {
        display: flex;
        width: fit-content;
        flex-direction: column;
        margin-left: auto;
        margin-right: auto;
        padding: 1rem;
    }

    .stanza {
        margin-bottom: 1rem;
        padding: 0.5rem;

        &:hover, &:focus-within {
            background-color: #F5F5F5;
        }
    }

    .line {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0.25rem 0.5rem;

        .right-menu {
            display: flex;
            align-items: center;
            float: right;
            margin-left: 1rem;
            opacity: 0;
        }

        &:hover, &:focus, &:focus-within {
            background-color: #E5E5E5;

            .right-menu {
                opacity: 1;
            }
        }
    }

    h2 {
        margin-left: 2.25rem;
    }
</style>