<script>
    import { createEventDispatcher } from 'svelte';
    import IconButton from "./IconButton.svelte";
    import Word from "./Word.svelte";
    import { push, remove } from "./common/arrays";
    import { getRhymes } from "./api/rhymes";
    import { clickOutside } from './actions/clickOutside';
    import { isPunctuationMark } from "./common/strings";

    export let stanzas = [];

    export let stanzaIndex = -1;
    export let lineIndex = -1;
    export let wordIndex = -1;
    let wordPos = 0;

    const dispatch = createEventDispatcher();

    const title = 'Стихотворение';
    const addLineTitle = 'Добавить строку';
    const removeLineTitle = 'Удалить строку';
    const getRhymeTitle = 'Подобрать рифму';
    const chooseTargetTooltip = 'Выберите слово, к которому нужно подобрать рифму';
    const chooseRhymeTooltip = 'Выберите рифму из списка';
    const chooseStressTooltip = 'Укажите ударение';
    const rhymeConfirm = 'Готово';
    const rhymeReject = 'Закрыть';
    const noRhymes = 'Не найдено';

    let chooseWord = false;
    let targetLineIndex = -1;
    let chosenLineIndex = -1;
    let chosenWordIndex = -1;

    let rhymesGot = false;
    let chooseStress = false;
    let rhymes;
    let selectedRhyme;

    //TODO: set correct word pos

    const onFocus = (s, l, w) => {
        if (chooseWord) {
            // if within this stanza update selection
            if (s === stanzaIndex) {
                chosenLineIndex = l;
                chosenWordIndex = w;
            } else { // else update target line to get rhymes to
                targetLineIndex = l;
                setChosenLineIndex(l, getStanza(s).length);
                setChosenWordIndex(s);
            }
        }
        setIndexes(s, l, w);
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
        if (chooseWord || rhymesGot) {
            return;
        }

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
        if (chooseWord || rhymesGot) {
            return;
        }
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

    const onEditFinished = (e, s, l, w) => {
        const { word } = e.detail;
        setIndexes(s, l, w);
        updateWord(word);
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
            // console.log('idxs', stanzaIndex, lineIndex, wordIndex)
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

    // TODO: debounce
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
            setWord(word, w, l, s);
        } else {
            removeWord();
            if (w > 0) {
                setWordIndex(--w);
            }
        }
    };

    // change args order
    const onAddLine = (s = stanzaIndex, l, newLine) => {
        if (chooseWord || rhymesGot) {
            return;
        }

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

    const onGetRhymes = async (stressPos) => {
        const word = getWord(chosenWordIndex, chosenLineIndex, stanzaIndex);
        let response = await getRhymes(word, stressPos);
        if (response.data) {
            rhymes = response.data;
            rhymesGot = true;
            chooseWord = false;
            if (rhymes.length) {
                selectedRhyme = rhymes[0].toLowerCase();
            }
        } else if (response.error === 'Stress needed') {
            chooseStress = true;
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
            setStanzaIndex(s - 1);
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
            setStanza([ [ '' ] ], 0);
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

    const setLastWord = (word, l = lineIndex, s = stanzaIndex) => {
        const line = getLine(l, s);
        if (line) {
            line[line.length - 1] = word;
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
        setLine(push(getLine(l, s), w, word), l, s);
    };

    const removeWord = (w = wordIndex, l = lineIndex, s = stanzaIndex) => {
        const line = getLine(l, s);
        if (line) {
            remove(line, w);
            stanzas = stanzas;
        }
    };

    const setIndexes = (s, l, w) => {
        // console.log('idx', s, l, w);
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

    const setChosenLineIndex = (l = lineIndex, stanzaLength = getStanza().length) => {
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
    };

    const setChosenWordIndex = (s = stanzaIndex) => {
        let isPunct = false;
        let w = getLine(chosenLineIndex, s).length;

        // find word
        do {
            const word = getWord(--w, chosenLineIndex, s);
            isPunct = isPunctuationMark(word);

            // if there's no words in line, go to previous line
            if (w < 0) {
                w = 0;
                // if the first line in stanza skip
                if (chosenLineIndex === 0) {
                    return;
                }
                chosenLineIndex--;
            }
        } while (isPunct && w > 0);

        chosenWordIndex = w;
    };

    const onBulbClick = (s, l) => {
        if (!rhymesGot) {
            setIndexes(s, l);
            chooseWord = !(chooseWord && l === targetLineIndex);
            wordPos = 0;
            targetLineIndex = l;

            // set chosen rhyme line
            setChosenLineIndex();
            setChosenWordIndex();
        }
    };

    const onClickOutsideStanza = (s) => {
        if (s === stanzaIndex) {
            chooseWord = false;
            wordPos = 0;
            rhymesGot = false;
            rhymes = [];
            console.log('outs');
            chooseStress = false;
            selectedRhyme = '';
        }
    };

    const onConfirmRhymes = () => {
        // TODO: move to states machine-like maybe??
        if (rhymesGot) {
            console.log('add r', selectedRhyme)
            addWord(selectedRhyme, getLine(targetLineIndex).length - 1, targetLineIndex);
            rhymesGot = false;
            setIndexes(stanzaIndex, targetLineIndex, wordIndex);
        } else if (chooseStress) {
            onGetRhymes(wordPos);
        } else {
            onGetRhymes();
        }
        chooseStress = false;
        selectedRhyme = '';
    };

    const onRejectRhymes = () => {
        if (rhymesGot) {
            console.log('rej');
            rhymesGot = false;
        } else {
            chooseWord = false;
        }
        wordPos = 0;
        chooseStress = false;
        selectedRhyme = '';
    };

    $: isLineChosenForRhymes = (s, l) => {
        return (chooseWord || rhymesGot) && s === stanzaIndex && l === targetLineIndex;
    };

</script>

<div class="poem">
    <h2 contenteditable>
        {title}
    </h2>
    {#each stanzas as lines, s}
        <div class="stanza"
             use:clickOutside
             on:clickOutside={() => onClickOutsideStanza(s)}>
            {#each lines as words, l}
                <div class="line">
                    <div>
                        {#each words as word, w}
                            <Word
                                    {word}
                                    bind:pos={wordPos}
                                    chosen={chooseWord && s === stanzaIndex && l === chosenLineIndex && w === chosenWordIndex}
                                    focused={!chooseWord && !rhymesGot && s === stanzaIndex && l === lineIndex && w === wordIndex}
                                    {chooseStress}
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
                                    on:editFinished={e => onEditFinished(e, s, l, w)}/>
                        {/each}
                        {#if rhymesGot && l === targetLineIndex && s === stanzaIndex}
                            <select bind:value={selectedRhyme}>
                                {#if rhymes.length}
                                    {#each rhymes as rhyme}
                                        <option>{rhyme.toLowerCase()}</option>
                                    {/each}
                                {:else}
                                    <option>{noRhymes}</option>
                                {/if}
                            </select>
                        {/if}
                    </div>

                    <div class="right-menu">
                        <div class="bulb-container">
                            <div class="menu-button" class:active={isLineChosenForRhymes(s, l)}>
                                <IconButton
                                        icon="bulb" size="18" padding="4"
                                        changeOpacity={!isLineChosenForRhymes(s, l)} title={getRhymeTitle}
                                        on:click={() => onBulbClick(s, l)}/>
                            </div>
                        </div>
                        {#if isLineChosenForRhymes(s, l)}
                            <div class="tooltip">
                                <div class="tooltip-arrow"></div>
                                {#if rhymesGot}
                                    {chooseRhymeTooltip}
                                {:else}
                                    {#if chooseStress}
                                        {chooseStressTooltip}
                                    {:else}
                                        {chooseTargetTooltip}
                                    {/if}
                                {/if}
                                <div class="tooltip-bar">
                                    {#if !rhymesGot || rhymes.length}
                                        <IconButton icon="check" size="10" padding="2" title={rhymeConfirm}
                                                    text={rhymeConfirm} borders color="#526933"
                                                    on:click={onConfirmRhymes}/>
                                    {/if}
                                    <IconButton icon="cross" size="10" padding="2" title={rhymeReject}
                                                text={rhymeReject} borders
                                                on:click={onRejectRhymes}/>
                                </div>
                            </div>
                        {/if}
                        <div class="menu-button">
                            <IconButton icon="cross" size="18" padding="4" title={removeLineTitle}
                                        changeOpacity on:click={() => onRemoveLine(s, l)}/>
                        </div>
                        <div class="menu-button">
                            <IconButton icon="plus" size="18" padding="4" title={addLineTitle}
                                        changeOpacity on:click={() => onAddLine(s, l)}/>
                        </div>
                    </div>
                </div>
            {/each}
        </div>
    {/each}
</div>

<style lang="scss">
    .tooltip {
        --color: #bc8f8fee;

        position: absolute;
        transform: translate(0, -100%);
        top: 0;
        left: -0.25rem;
        min-width: 15rem;
        background-color: var(--color);
        box-shadow: none;
        padding: 0.5rem 0.25rem 0.25rem 0.5rem;
        text-align: left;
        font-size: .8em;
        z-index: 2;
        box-sizing: border-box;
        border-radius: 0.25rem;
        font-style: italic;

        & .tooltip-arrow {
            &::before {
                border-width: 0.6rem 0.45rem 0 0.45rem;
                border-left-color: transparent;
                border-right-color: transparent;
                border-bottom-color: transparent;
                top: calc(100%);
                content: '';
                display: block;
                width: 0;
                height: 0;
                border-style: solid;
                position: absolute;
                color: var(--color);
            }
        }

        & .tooltip-bar {
            display: flex;
            justify-content: space-around;
            margin-top: 0.25rem;
        }

        & .tooltip-button {
            background: none;
            height: 1.5rem;
            line-height: 1rem;
            padding: 0.25rem;
            color: #500808;
            border: none;

            &:hover {
                border-bottom: 1px solid #500808;
            }
        }
    }

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

        &:hover, &:focus, &:focus-within {
            background-color: #E5E5E5;

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

    h2 {
        margin-left: 2.25rem;
    }
</style>