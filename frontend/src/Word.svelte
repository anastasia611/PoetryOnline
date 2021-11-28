<script>
    import RemoveButton from './IconButton.svelte';
    import { createEventDispatcher, afterUpdate } from 'svelte';
    import { isLetter, isPunctuationMark } from './common/strings';
    import tabFocus from './actions/tabFocus';
    import {
        getCaretCharacterOffsetWithin,
        setCaretPosition,
    } from './common/dom';
    import { isPortraitOrientation } from './common/dom';

    export let word = '';
    export let focused = false;
    export let pos = 0;
    export let chooseStress = false;
    export let chosen = false;
    export let editable = false;

    const title = 'Удалить слово';
    const dispatch = createEventDispatcher();

    let tabFocused = false;
    let wordElement;
    let lettersElement;

    let oldPos = pos;
    let curPos = pos;

    const setPos = (newPos, allowAfter = true) => {
        console.log('setcp', curPos, newPos, word);
        // why large numbers??
        if (newPos > word.length) {
            return;
        }
        if (newPos < 0) {
            newPos = 0;
        } else {
            const maxVal = allowAfter ? word.length : word.length - 1;
            if (newPos > maxVal) {
                newPos = maxVal;
            }
        }
        curPos = newPos;
        console.log('SET POS', word, curPos);
        if (chooseStress) {
            pos = curPos;
        }
    };

    const onRemove = () => {
        dispatch('remove');
    };

    const onRemoveBefore = () => {
        dispatch('remove-before', {
            word,
            punct: isPunctuationMark(word),
        });
    };

    const onRemoveAfter = () => {
        dispatch('remove-after', { word });
    };

    document.addEventListener('keydown', (e) => {
        if (chooseStress) {
            e.preventDefault();

            if (e.key === 'ArrowLeft') {
                console.log('POS', curPos - 1);
                setPos(curPos - 1, false);
            } else if (e.key === 'ArrowRight') {
                setPos(curPos + 1, false);
            }
        }
    });

    const onKeyPress = (e) => {
        console.log('key pr', e);
        const old = word;
        word = getWordContent();

        if (chosen) {
            e.preventDefault();
        } else {
            setPos(getCaretCharacterOffsetWithin(wordElement));

            if (!isLetter(e.key) || isPunctuationMark(word)) {
                e.preventDefault();
            }

            if (e.key === 'Enter') {
                dispatch('enter', { word, pos: curPos });
            } else if (isPunctuationMark(e.key)) {
                dispatch('punct', { word, sign: e.key });
            } else if (e.key === ' ') {
                if (word.length === 1) {
                    word = word.replace('-', '—');
                }
                dispatch('space', { word, pos: curPos });
            }
        }
    };

    const onKeyDown = (e) => {
        console.log('key d', e, e.key, curPos);
        const old = word;
        word = getWordContent();

        if (e.key === ' ') {
            e.preventDefault();
        }

        if (focused && !chooseStress && !chosen) {
            console.log(
                'key down',
                getCaretCharacterOffsetWithin(wordElement),
                word,
                word.length,
                word[0]
            );
            setPos(getCaretCharacterOffsetWithin(wordElement));

            if (e.key === ' ') {
                if (word.length === 1) {
                    console.log('content repl', old, word);
                    word = word.replace('-', '—');
                }
                dispatch('space', { word, pos: curPos });
            } else if (e.key === 'ArrowLeft' && curPos === 0) {
                e.preventDefault();
                dispatch('back');
            } else if (e.key === 'ArrowRight' && curPos === word.length) {
                e.preventDefault();
                dispatch('next');
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                dispatch('up');
            } else if (e.key === 'ArrowDown') {
                e.preventDefault();
                dispatch('down');
            } else if (e.key === 'Backspace' || e.key === 'Delete') {
                console.log('DEL', word, e.key, curPos);
                if (!word.length) {
                    // to prevent deletion of symbol in next word
                    console.log('DEL empty');
                    e.preventDefault();
                    onRemove();
                } else if (e.key === 'Backspace' && curPos === 0) {
                    console.log('DEL before', word);
                    e.preventDefault();
                    onRemoveBefore();
                } else if (e.key === 'Delete' && curPos === word.length) {
                    console.log('DEL after', word);
                    e.preventDefault();
                    onRemoveAfter();
                }
            }
        }
    };

    const getWordContent = () => {
        if (chooseStress || chosen) {
            return word;
        }
        return (wordElement && wordElement.textContent) || '';
    };

    const onClick = () => {
        console.log('CLICK', word);
        if (!chooseStress) {
            setPos(getCaretCharacterOffsetWithin(wordElement));
            // setCaretPosition(wordElement, curPos);
            // console.log('CLICK SET CAR',word, curPos)
        }
    };

    const onFocus = () => {
        console.log('On focus', word);
        if (!chosen && !chooseStress) {
            dispatch('focus');
        }
        focused = true;
    };

    const onBlur = () => {
        if (editable) {
            dispatch('edit-finished', { word });
        }

        // may be removed when choosing stress
        if (wordElement) {
            wordElement.textContent = word.toLowerCase();
        }
        tabFocused = false;
        focused = false;

        oldPos = -1;
        // console.trace()
        console.log('unfoc');
        // TODO: needed?
        dispatch('blur', { word: getWordContent() });
    };

    const onTabFocus = () => {
        if (!chosen && !chooseStress) {
            tabFocused = true;
        }
    };
// TODO: in strings utility fix parsing new stanza <-> empty line, etc.
    const onPaste = (e) => {
        let paste = (e.clipboardData || window.clipboardData).getData('text');
        paste = paste.replaceAll('\n', '\r\n')
        const selection = window.getSelection();
        if (!selection.rangeCount) { 
            return false; 
        }
        selection.deleteFromDocument();
        selection.getRangeAt(0).insertNode(document.createTextNode(paste));

        e.preventDefault();

        dispatch('paste', { content: wordElement.textContent.replaceAll('\r\n', '\n') });
    };

    const onInput = (e) => {
        console.log(
            'input',
            word,
            wordElement.textContent,
            wordElement.textContent.length,
            pos,
            e
        );
        word = wordElement.textContent;
        // if (pos > 0) {
        //     setPos(pos - 1);
        // }
        dispatch('change', { word, pos: curPos });
    };

    const onClickLetter = (p) => {
        console.log('letter', word, p);
        setPos(p, false);
    };

    $: if (focused && wordElement) {
        curPos = pos;
        console.log('focus word', word);
        if (!chosen) {
            wordElement.focus();
        }
    }

    $: if (chooseStress && lettersElement) {
        lettersElement.focus();
    }

    afterUpdate(() => {
        if (editable && wordElement) {
            const old = wordElement.textContent;
            if (word !== wordElement.textContent) {
                wordElement.textContent = word;
            }

            if (focused) {
                wordElement.focus();
                if (pos <= word.length && pos !== oldPos) {
                    console.log(
                        'set car pos',
                        old,
                        word,
                        oldPos,
                        pos,
                        curPos,
                        wordElement.textContent
                    );
                    setCaretPosition(wordElement, pos);
                    setPos(pos);
                }
                oldPos = pos;
            }
        }
    });
</script>

<div class="word-container" on:click|stopPropagation={onFocus}>
    <div class="word" class:chosen>
        {#if !isPortraitOrientation()}
            <div class="remove">
                <RemoveButton
                    icon="cross"
                    disabled={!editable}
                    iconSize="10"
                    {title}
                    on:click={onRemove}
                    on:touchend={onRemove}
                />
            </div>
        {/if}

        {#if chosen && chooseStress}
            <div
                role="textbox"
                tabindex="0"
                title={word}
                class="word-text"
                class:key-focus-visible={tabFocused}
                use:tabFocus
                bind:this={lettersElement}
                on:keydown={onKeyDown}
                on:tabfocus={onTabFocus}
                on:focus={onFocus}
                on:blur={onBlur}
            >
                {#each word as letter, i}
                    {#if i === curPos}
                        <b>{letter}</b>
                    {:else}
                        <span
                            class="letter"
                            on:click={() => onClickLetter(i)}
                            on:touchend={() => onClickLetter(i)}
                        >
                            {letter}
                        </span>
                    {/if}
                {/each}
            </div>
        {:else}
            <div
                role="textbox"
                tabindex="0"
                title={word}
                contenteditable={editable}
                spellcheck="false"
                class="word-text"
                on:input={onInput}
                bind:this={wordElement}
                class:key-focus-visible={tabFocused}
                use:tabFocus
                on:keypress={onKeyPress}
                on:keydown={onKeyDown}
                on:click={onClick}
                on:tabfocus={onTabFocus}
                on:focus={onFocus}
                on:blur={onBlur}
                on:paste={onPaste}
            >
                {word.toLowerCase()}
            </div>
        {/if}
    </div>
</div>

<style lang="scss">
    @import './styles/_mixins';

    .word-container {
        display: inline-flex;
        //padding: 0.25rem 0.5rem;
        height: 100%;
        white-space: nowrap;

        &:hover,
        &:focus,
        &:focus-within {
            & .word {
                background-color: #d0d0d0;
                outline: none;

                & .remove {
                    opacity: 1;
                }
            }
        }
    }

    .word {
        --size: 1rem;

        display: inline-flex;
        padding: 0.2rem 0.125rem;
        line-height: var(--size);
        color: #222;
        cursor: default;
        border-radius: 0.125rem;
        background-clip: padding-box;
        user-select: none;

        & .remove {
            display: flex;
            opacity: 0;
            margin-right: 0.125rem;
        }

        &:hover,
        &:focus,
        &:focus-within {
            background-color: #d0d0d0;
            outline: none;

            & .remove {
                opacity: 1;
            }
        }

        &.chosen {
            box-shadow: 0 0 0.25rem 0.25rem #500808aa;
            //outline: #500808AA 0.125rem solid;
        }
    }

    .word-text {
        display: inline;
        color: #500808;
        border: none;
        font-size: var(--size);
        line-height: var(--size);
        min-width: var(--size);
        background: none;
        padding-right: 0.125rem;
        white-space: pre-line;

        &:focus {
            outline: none;
        }

        &.key-focus-visible {
            outline: #888888 0.125rem solid;
        }
    }

    .letter {
        display: inline-block;
        width: fit-content;
    }

    @include portrait {
        .word {
            padding: 0.125rem 0.125rem;

            & .word-text {
                //height: 0.75rem;
                font-size: medium;
                min-width: 0.125rem;
                padding-right: 0;
                margin: 0 0.125rem;
            }

            & .remove {
                margin-right: 0;
            }
        }
    }
</style>
