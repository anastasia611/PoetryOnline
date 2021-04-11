<script>
    import RemoveButton from "./IconButton.svelte";
    import { createEventDispatcher } from 'svelte';
    import { isLetter, isPunctuationMark } from "./common/strings";
    import tabFocus from "./actions/tabFocus";

    export let word = "";
    export let editable = false;

    const title = 'Удалить слово';

    let invisible = !editable;
    let focused = false;
    let input;
    $: width = 0;

    const dispatch = createEventDispatcher();

    const onRemove = () => {
        dispatch('removeWord');
    };

    $: if (input) {
        input.addEventListener("keypress", e => {
            onKeyPress(e);
            e.target.setSelectionRange(0, 0);
        });
    }

    const onKeyPress = e => {
        if (!isLetter(e.key) && !isPunctuationMark(e.key) && e.key !== ' ') {
            e.preventDefault();
        }

        if (e.key === "Enter") {
            dispatch('enter', { word: input.value });
            invisible = true;
            editable = false;
        } else if (isPunctuationMark(e.key) || e.key === " ") {
            dispatch('punct', { word: input.value, sign: e.key });
            invisible = true;
            editable = false;
        }
    };

    const onKeyDown = (e) => {
        const pos = e.target.selectionStart + 1;
        console.log(e.key, pos);
        if (e.key === 'ArrowLeft' && pos === 1) {
            dispatch('back');
        } else if (e.key === 'ArrowRight' && pos === word.length + 1) {
            dispatch('next');
        }
    };

    const onFocus = () => {
        editable = true;
    };

    const onBlur = () => {
        if (!word) {
            dispatch('removeWord');
        } else {
            dispatch('editFinished', { word: input.value });
        }

        editable = false;
        invisible = true;
    };

</script>

{#if editable}
    <input
            class="word-editable"
            value={word}
            style="width:{width}px;"
            autofocus
            autocomplete="on"
            autocapitalize="off"
            spellcheck="true"
            placeholder=""
            bind:this={input}
            on:keydown={onKeyDown}
            on:blur={onBlur}
    />
{:else}

    <div class="word" tabindex="-1"
         bind:clientWidth={width}
         class:key-focus-visible={focused}
         use:tabFocus
         on:tabfocus={() => {console.log('FOC');focused = true}}
         on:focus={onFocus}
         on:mouseover={() => invisible = false}
         on:mouseleave={() => invisible = true}>

        <span class="word-text" title={word}>
            {word}
        </span>

        <span class:invisible class="remove">
            <RemoveButton icon="cross" size="10" {title} on:click={onRemove}/>
        </span>
    </div>
{/if}

<style lang="scss">
    .word {
        display: inline-block;
        width: fit-content;
        padding: 0.5rem;
        line-height: 1rem;
        color: #222;
        cursor: default;
        border-radius: 0.125rem;
        background-clip: padding-box;
        user-select: none;

        &:hover, &:focus {
            background-color: #dddddd;
            outline: none;
        }

        &.key-focus-visible {
            background-color: gray;
            border: #666666 2px solid;
        }
    }

    .word-text {
        color: #500808;
    }

    .word-editable {
        border: none;
        padding: 0 0.5rem;
        height: 2rem;

        &:focus {
            outline: none;
        }
    }

    .remove {
        &.invisible {
            opacity: 0;
        }

        &:focus-within {
            opacity: 1;
        }
    }

</style>