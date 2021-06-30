<script>
    import RemoveButton from "./IconButton.svelte";
    import { createEventDispatcher, onDestroy, onMount } from 'svelte';
    import { isLetter, isPunctuationMark } from "./common/strings";
    import tabFocus from "./actions/tabFocus";
    import { getCaretCharacterOffsetWithin, setCaretPosition } from "./common/dom";

    export let word = '';
    export let focused = false;
    export let pos = 0;
    export let chooseStress = false;
    export let chosen = false;

    const title = 'Удалить слово';
    const dispatch = createEventDispatcher();

    let tabFocused = false;
    let wordElement;
    let textBeforeStress;
    let textAfterStress;
    let stressSymbol;

    let orig = word;
    let destroyed = false;

    const setPos = newPos => {
        if (newPos < 0) {
            newPos = 0;
        } else if (newPos > word.length - 1) {
            newPos = word.length - 1;
        }
        pos = newPos;
    };

    const onRemove = () => {
        dispatch('remove');
    };

    const onRemoveBefore = () => {
        dispatch('remove-before', {
            word,
            punct: isPunctuationMark(word)
        });
    };

    const onRemoveAfter = () => {
        dispatch('remove-after', { word });
    };

    const onKeyPress = e => {
        console.log('keypr')
        word = getWordContent();

        if (chosen) {
            e.preventDefault();
        } else {
            pos = getCaretCharacterOffsetWithin(wordElement);

            if (!isLetter(e.key) || isPunctuationMark(word)) {
                e.preventDefault();
            }

            if (e.key === 'Enter') {
                dispatch('enter', { word, pos });
            } else if (isPunctuationMark(e.key)) {
                dispatch('punct', { word, sign: e.key });
            } else if (e.key === ' ') {
                if (word.length === 1) {
                    word = word.replace('-', '—');
                }
                dispatch('space', { word, pos });
            }

            word = getWordContent();
        }
    };

    const onKeyDown = (e) => {
        console.log('keydwn')
        word = getWordContent();

        if (e.key === ' ') {
            e.preventDefault();
        }

        if (chosen) {
            e.preventDefault();

            if (e.key === 'ArrowLeft') {
                setPos(pos - 1);
            } else if (e.key === 'ArrowRight') {
                setPos(pos + 1);
            }
        } else {
            pos = getCaretCharacterOffsetWithin(wordElement);

            if (e.key === ' ') {
                if (word.length === 1) {
                    word = word.replace('-', '—');
                }
                dispatch('space', { word, pos });
            } else if (e.key === 'ArrowLeft' && pos === 0) {
                e.preventDefault();
                dispatch('back');
            } else if (e.key === 'ArrowRight' && pos === word.length) {
                e.preventDefault();
                dispatch('next');
            } else if (e.key === 'ArrowUp') {
                dispatch('up');
            } else if (e.key === 'ArrowDown') {
                dispatch('down');
            } else if (e.key === 'Backspace' || e.key === 'Delete') {
                console.log('DEL', word, word.length)
                if (!word.length) {
                    // to prevent deletion of symbol in next word
                    e.preventDefault();
                    onRemove();
                } else if (e.key === 'Backspace' && pos === 0) {
                    e.preventDefault();
                    onRemoveBefore();
                } else if (e.key === 'Delete' && pos === word.length) {
                    e.preventDefault();
                    onRemoveAfter();
                }
            }
        }
    };

    const getWordContent = () => {
        return wordElement.textContent || '';
    };

    const onClick = () => {
        console.log('click')
        setPos(getCaretCharacterOffsetWithin(wordElement));
    };

    const onFocus = () => {
        dispatch('focus');
    };

    const onBlur = () => {
        if (destroyed) {
            return;
        }
        if (word) {
            if (word.length < 2) {
                word = word.replace('-', '—');
                // workaround for - (can be set only after editing finished)
                wordElement.textContent = word;
            }
            dispatch('editFinished', { word });
        }

        tabFocused = false;
        // TODO: needed?
        dispatch('blur', { word: getWordContent() });
    };

    onDestroy(() => {
        destroyed = true;
        if (wordElement) {
            // wordElement.removeEventListener('keypress', onKeyPress);
            // wordElement.removeEventListener('keydown', onKeyDown);
        }
    });

    onMount(async () => {
        // wordElement.addEventListener('keypress', onKeyPress);
        // wordElement.addEventListener('keydown', onKeyDown);
    });

    const onInput = () => {
        word = wordElement.textContent;
    };

    $: {
        // TODO: empty word
        textBeforeStress = word.substr(0, pos);
        stressSymbol = word[pos];
        textAfterStress = word.substr(pos + 1);
        // console.log(pos, textBeforeStress, stressSymbol, textAfterStress)
    }
    $: if (wordElement && (focused || chosen)) {
        wordElement.focus();
    }
    $: if (wordElement) {
        // console.log('Cursor', word, pos)
        setCaretPosition(wordElement, pos);
    }

</script>

<div class="word"
     class:chosen={chosen}
     on:click={onFocus}>

    {#if !chosen}
        <div class="remove">
            <RemoveButton icon="cross" size="10" {title} changeOpacity on:click={onRemove}/>
        </div>
    {/if}

    <div role="textbox"
         title={word}
         contenteditable={chooseStress || !chosen}
         class="word-text"
         on:input={onInput}
         bind:this={wordElement}
         class:key-focus-visible={tabFocused}
         use:tabFocus
         on:keypress={onKeyPress}
         on:keydown={onKeyDown}
         on:click={onClick}
         on:tabfocus={() => tabFocused = true}
         on:focus={onFocus}
         on:blur={onBlur}
    >
        {#if chosen && chooseStress}
            <!--{word}-->
            {textBeforeStress}<b>{stressSymbol}</b>{textAfterStress}
        {:else}
            {word}
        {/if}
    </div>
</div>

<style lang="scss">
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

        &:hover, &:focus, &:focus-within {
            background-color: #D0D0D0;
            outline: none;

            & .remove {
                opacity: 1;
            }
        }

        &.chosen {
            border: #500808AA solid 0.125rem;
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

        &:focus {
            outline: none;
        }

        &.key-focus-visible {
            outline: #888888 0.125rem solid;
        }
    }
</style>