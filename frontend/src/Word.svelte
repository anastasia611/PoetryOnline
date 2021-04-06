<script>
    import RemoveButton from "./RemoveButton.svelte";
    import {createEventDispatcher} from 'svelte';
    import {isLetter, isPunctuationMark} from "./common/strings";

    export let word = "";
    export let editable = false;

    const title = 'Удалить слово';

    let invisible = !editable;
    $: width = 0;

    const dispatch = createEventDispatcher();

    const onRemove = () => {
        dispatch('removeWord');
    };

    const onKeyPress = e => {
        if (e.key === "Enter") {
            dispatch('addWord', { word });
            invisible = true;
            editable = false;
        } else if (isPunctuationMark(e.key) || e.key === " ") {
            dispatch('addWord', { word, newWord: e.key });
            invisible = true;
            editable = false;
        } else if (isLetter(e.key)) {
            const pos = e.target.selectionStart;
            word = word.substr(0, pos) + e.key + word.substr(pos, word.length);
        } else if (e.key === 'Backspace' || e.key === 'Delete') {
            word = word.substr(0, word.length - 1);
        } else if (e.key === 'ArrowLeft') {
            const pos = e.target.selectionStart - 1;
            e.target.setSelectionRange(pos, pos);
        } else if (e.key === 'ArrowRight') {
            const pos = e.target.selectionStart + 1;
            e.target.setSelectionRange(pos, pos);
        }
    };

    const onBlur = () => {
        if (!word) {
            dispatch('removeWord');
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
            placeholder="введите слово.."
            on:keydown|preventDefault={onKeyPress}
            on:blur={onBlur}
    />
{:else}

    <div class="word" tabindex="-1"
         bind:clientWidth={width}
         on:focus={() => editable = true}
         on:mouseover={() => invisible = false}
         on:mouseleave={() => invisible = true}>

        <span class="word-text" title={word}>
            {word}
        </span>

        <span class:invisible class="remove">
            <RemoveButton size="10" {title} on:click={onRemove}/>
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
        /*background-color: #F0CBCB;*/

        &:hover, &:focus {
            background-color: #dddddd;
            outline: none;
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
    }
</style>