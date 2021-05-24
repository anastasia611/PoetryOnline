<script>
    import RemoveButton from "./IconButton.svelte";
    import { createEventDispatcher, onDestroy, onMount } from 'svelte';
    import { isLetter, isPunctuationMark } from "./common/strings";
    import tabFocus from "./actions/tabFocus";
    import { convertPixelsToRem, setCaretPosition } from "./common/dom";

    export let word = '';
    export let editable = false;
    export let pos = 0;
    export let chosen = false;

    const title = 'Удалить слово';
    const dispatch = createEventDispatcher();

    let tabFocused = false;
    let wordElement;
    let hidden;

    let orig = word;
    let destroyed = false;
    let width = 0;

    $: if (chosen) {
        console.log(chosen, word)
    }

    const onRemove = () => {
        console.log('rm word', word)
        dispatch('remove');
    };

    const onRemoveBefore = () => {
        console.log('rm word bef', word)
        dispatch('remove-before', { word, punct: isPunctuationMark(word) });
    };

    const onRemoveAfter = () => {
        console.log('rm word aft', word)
        dispatch('remove-after', { word });
    };

    const onKeyPress = e => {
        const pos = e.target.selectionEnd;
        word = getWordContent();

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
    };

    const onKeyDown = (e) => {
        const pos = e.target.selectionEnd;
        word = getWordContent();

        if (e.key === 'ArrowLeft' && pos === 0) {
            dispatch('back');
        } else if (e.key === 'ArrowRight' && pos === word.length) {
            dispatch('next');
        } else if (e.key === 'ArrowUp') {
            dispatch('up');
        } else if (e.key === 'ArrowDown') {
            dispatch('down');
        } else if (e.key === 'Backspace' || e.key === 'Delete') {
            if (!word.length) {
                // to prevent deletion of symbol in next word
                console.log('rm word on press')
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
    };

    const getWordContent = () => {
        // return (wordElement.textContent || '').trim();
        return wordElement.value || '';
    };

    const onFocus = () => {
        console.log('foc', wordElement.value)
        dispatch('focus');
    };

    const onBlur = () => {
        if (destroyed) {
            return;
        }
        if (!word) {
            // console.log('rm in word', orig, dstr)
            // onRemove();
        } else {
            dispatch('editFinished', { word });
        }

        tabFocused = false;
        console.log('blur', word)
        dispatch('blur', { word: getWordContent() });
    };

    onDestroy(() => {
        destroyed = true;
        if (wordElement) {
            wordElement.removeEventListener('keypress', onKeyPress);
            wordElement.removeEventListener('keydown', onKeyDown);
        }
    });

    onMount(async () => {
        // console.log('mount', word)
        wordElement.addEventListener('keypress', onKeyPress);
        wordElement.addEventListener('keydown', onKeyDown);
        hidden.innerHTML = word;
    });

    const onInput = () => {
        word = wordElement.value;
        hidden.innerHTML = wordElement.value;
        width = convertPixelsToRem(hidden.clientWidth) + 0.25 + 'rem';
    };

    $: if (wordElement) {
        hidden.innerHTML = word;
        width = convertPixelsToRem(hidden.clientWidth) + 0.25 + 'rem';
    }
    $: if (wordElement && editable) {
        console.log(word, editable)
        wordElement.focus();
    }
    $: if (wordElement && pos) {
        console.log('Cursor',word, pos)
        setCaretPosition(wordElement, 0);
    }

</script>

<div class="word"
     class:chosen={chosen}
     on:click={onFocus}>

    <div class="remove">
        <RemoveButton icon="cross" size="10" {title} changeOpacity on:click={onRemove}/>
    </div>

    <input
            title={word}
            class="word-text"
            value={word}
            style="width: {width}"
            on:input={onInput}
            bind:this={wordElement}
            class:key-focus-visible={tabFocused}
            use:tabFocus
            on:tabfocus={() => tabFocused = true}
            on:focus={onFocus}
            on:blur={onBlur}
    />
    <div class="hidden"
         bind:this={hidden}>
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
        color: #500808;
        border: none;
        font-size: var(--size);
        line-height: var(--size);
        min-width: var(--size);
        background: none;
        padding: 0;

        &:focus {
            outline: none;
        }

        &.key-focus-visible {
            outline: #888888 0.125rem solid;
        }
    }

    .hidden {
        position: absolute;
        visibility: hidden;
    }

</style>